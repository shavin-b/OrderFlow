package com.orderflow.service;

import com.orderflow.dto.automation.GreetingDto;
import com.orderflow.entity.*;
import com.orderflow.repository.AutomationRuleRepository;
import com.orderflow.repository.MessageRepository;
import com.orderflow.repository.QueuedMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutomationEngineServiceTest {

    @Mock
    private AutomationRuleRepository ruleRepository;
    @Mock
    private KeywordMatcherService keywordMatcherService;
    @Mock
    private BusinessHoursService businessHoursService;
    @Mock
    private GreetingService greetingService;
    @Mock
    private WhatsAppService whatsAppService;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private QueuedMessageRepository queuedMessageRepository;
    @Mock
    private TaskScheduler taskScheduler;

    @InjectMocks
    private AutomationEngineService automationEngineService;

    private Customer customer;
    private Conversation conversation;
    private Message inboundMessage;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).phone("+15550001111").waId("15550001111").build();
        conversation = Conversation.builder().id(10L).customer(customer).build();
        inboundMessage = Message.builder().id(100L).conversation(conversation).body("help").build();
    }

    @Test
    @DisplayName("Should send greeting message on first conversation message")
    void testGreetingMessageSent() {
        when(messageRepository.countByConversationId(10L)).thenReturn(1L);
        when(greetingService.findActiveGreeting()).thenReturn(Optional.of(
                GreetingDto.builder().messageBody("Welcome to OrderFlow!").build()));
        when(businessHoursService.isWithinBusinessHours(any())).thenReturn(true);
        when(ruleRepository.findAllActiveWithKeywordsAndReplies()).thenReturn(List.of());
        when(whatsAppService.sendTextMessage(anyString(), anyString())).thenReturn("wamid.greeting1");

        automationEngineService.processInboundMessage(conversation, inboundMessage);

        verify(whatsAppService).sendTextMessage(eq("+15550001111"), eq("Welcome to OrderFlow!"));
    }

    @Test
    @DisplayName("Should send away message when outside business hours")
    void testAwayMessageSent() {
        when(messageRepository.countByConversationId(10L)).thenReturn(2L); // not first message
        when(businessHoursService.isWithinBusinessHours(any())).thenReturn(false);
        when(businessHoursService.getAwayMessage(any())).thenReturn(Optional.of("We are currently closed."));
        when(whatsAppService.sendTextMessage(anyString(), anyString())).thenReturn("wamid.away1");

        automationEngineService.processInboundMessage(conversation, inboundMessage);

        verify(whatsAppService).sendTextMessage(eq("+15550001111"), eq("We are currently closed."));
        verifyNoInteractions(ruleRepository);
    }

    @Test
    @DisplayName("Should trigger highest priority matching rule and execute reply")
    void testRuleMatchingAndExecution() {
        when(messageRepository.countByConversationId(10L)).thenReturn(2L);
        when(businessHoursService.isWithinBusinessHours(any())).thenReturn(true);

        Keyword kw = Keyword.builder().pattern("help").matchType(Keyword.MatchType.EXACT).build();
        Reply reply = Reply.builder().messageBody("How can we assist you?").delaySeconds(0).build();
        AutomationRule rule = AutomationRule.builder()
                .id(5L)
                .name("Help Rule")
                .priority(10)
                .cooldownSeconds(0)
                .triggerCount(0L)
                .keywords(List.of(kw))
                .replies(List.of(reply))
                .build();

        when(ruleRepository.findAllActiveWithKeywordsAndReplies()).thenReturn(List.of(rule));
        when(keywordMatcherService.matches(eq("help"), eq(kw))).thenReturn(true);
        when(whatsAppService.sendTextMessage(anyString(), anyString())).thenReturn("wamid.reply1");

        automationEngineService.processInboundMessage(conversation, inboundMessage);

        verify(whatsAppService).sendTextMessage(eq("+15550001111"), eq("How can we assist you?"));
        verify(ruleRepository).save(eq(rule));
    }
}
