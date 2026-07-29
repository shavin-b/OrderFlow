import { apiClient } from './client';
import { Subscription, SubscriptionPlan } from '../types';

export const subscriptionsApi = {
  getMySubscription: async (): Promise<Subscription> => {
    const res = await apiClient.get('/subscriptions/my-subscription');
    return res.data.data;
  },

  upgrade: async (planType: SubscriptionPlan, paymentRef?: string): Promise<Subscription> => {
    const res = await apiClient.post(
      `/subscriptions/upgrade?planType=${planType}${paymentRef ? `&paymentRef=${paymentRef}` : ''}`
    );
    return res.data.data;
  },
};
