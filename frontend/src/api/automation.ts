import { apiClient } from './client';
import { AutomationRule, BusinessHours, Greeting } from '../types';

export const automationApi = {
  // Rules & Keywords & Replies
  getRules: async (): Promise<AutomationRule[]> => {
    const res = await apiClient.get('/automation/rules');
    return res.data.data;
  },

  createRule: async (rule: Partial<AutomationRule>): Promise<AutomationRule> => {
    const res = await apiClient.post('/automation/rules', rule);
    return res.data.data;
  },

  updateRule: async (id: number, rule: Partial<AutomationRule>): Promise<AutomationRule> => {
    const res = await apiClient.put(`/automation/rules/${id}`, rule);
    return res.data.data;
  },

  toggleRuleActive: async (id: number, active: boolean): Promise<void> => {
    await apiClient.patch(`/automation/rules/${id}/active?active=${active}`);
  },

  deleteRule: async (id: number): Promise<void> => {
    await apiClient.delete(`/automation/rules/${id}`);
  },

  // Business Hours
  getBusinessHours: async (): Promise<BusinessHours[]> => {
    const res = await apiClient.get('/automation/business-hours');
    return res.data.data;
  },

  updateBusinessHours: async (data: BusinessHours): Promise<BusinessHours> => {
    const res = await apiClient.put('/automation/business-hours', data);
    return res.data.data;
  },

  // Greetings
  getGreetings: async (): Promise<Greeting[]> => {
    const res = await apiClient.get('/automation/greetings');
    return res.data.data;
  },

  createGreeting: async (data: Partial<Greeting>): Promise<Greeting> => {
    const res = await apiClient.post('/automation/greetings', data);
    return res.data.data;
  },
};
