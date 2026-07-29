export type UserRole = 'ROLE_ADMIN' | 'ROLE_MANAGER' | 'ROLE_SUPPORT' | 'ROLE_USER';

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';

export type SubscriptionPlan = 'TRIAL' | 'MONTHLY' | 'YEARLY' | 'LIFETIME';

export type SubscriptionStatus = 'ACTIVE' | 'TRIAL' | 'EXPIRED' | 'SUSPENDED';

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName?: string;
  phone?: string;
  emailVerified: boolean;
  status: UserStatus;
  roles: UserRole[];
  subscription?: Subscription;
  createdAt: string;
}

export interface Subscription {
  id: number;
  userId: number;
  planType: SubscriptionPlan;
  status: SubscriptionStatus;
  startDate: string;
  endDate: string;
  autoRenew: boolean;
  paymentRef?: string;
  isActive: boolean;
}

export interface Customer {
  id: number;
  waId: string;
  phone: string;
  name: string;
  email?: string;
  status: 'ACTIVE' | 'BLOCKED' | 'ARCHIVED';
  createdAt: string;
  updatedAt: string;
}

export type MatchType = 'CONTAINS' | 'EXACT' | 'STARTS_WITH' | 'ENDS_WITH' | 'REGEX';

export interface Keyword {
  id?: number;
  pattern: string;
  matchType: MatchType;
  ignoreCase: boolean;
}

export interface Reply {
  id?: number;
  messageBody: string;
  replyOrder: number;
  delaySeconds: number;
  mediaUrl?: string;
  mediaType?: string;
}

export interface AutomationRule {
  id?: number;
  name: string;
  description?: string;
  priority: number;
  active: boolean;
  cooldownSeconds: number;
  triggerCount?: number;
  keywords: Keyword[];
  replies: Reply[];
  createdAt?: string;
}

export interface DailyStat {
  statDate: string;
  incomingMessages: number;
  outgoingReplies: number;
  failedReplies: number;
  avgResponseTimeMs: number;
  activeCustomers: number;
  topKeyword?: string;
}

export interface MonthlyStat {
  yearMonth: string;
  incomingMessages: number;
  outgoingReplies: number;
  failedReplies: number;
  avgResponseTimeMs: number;
  activeCustomers: number;
  revenue: number;
}

export interface KeywordUsage {
  pattern: string;
  triggerCount: number;
}

export interface AnalyticsSummary {
  totalIncomingMessages: number;
  totalOutgoingReplies: number;
  totalFailedReplies: number;
  avgResponseTimeMs: number;
  activeCustomersCount: number;
  monthlyRevenue: number;
  successRatePercentage: number;
  topKeywordPattern: string;
  dailyBreakdown: DailyStat[];
  topKeywords: KeywordUsage[];
}

export interface Report {
  id: number;
  reportName: string;
  reportType: 'CSV' | 'EXCEL' | 'PDF';
  startDate: string;
  endDate: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED';
  downloadUrl?: string;
  createdAt: string;
}

export interface AuditLog {
  id: number;
  userId?: number;
  userEmail?: string;
  action: string;
  resource: string;
  details?: string;
  ipAddress?: string;
  timestamp: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: number;
  email: string;
  roles: UserRole[];
  subscriptionStatus: SubscriptionStatus;
}
