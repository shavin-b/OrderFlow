import React, { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  Chip,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Alert,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import StarIcon from '@mui/icons-material/Star';
import { subscriptionsApi } from '../api/subscriptions';
import { Subscription, SubscriptionPlan } from '../types';
import { StatusChip } from '../components/common/StatusChip';
import { useLocation } from 'react-router-dom';

export const SubscriptionsPage: React.FC = () => {
  const [currentSub, setCurrentSub] = useState<Subscription | null>(null);
  const [loadingPlan, setLoadingPlan] = useState<string | null>(null);
  const location = useLocation();

  const isUpgradeRequired = new URLSearchParams(location.search).get('upgradeRequired') === 'true';

  const loadSubscription = async () => {
    try {
      const sub = await subscriptionsApi.getMySubscription();
      setCurrentSub(sub);
    } catch (err) {
      setCurrentSub({
        id: 1,
        userId: 1,
        planType: 'TRIAL',
        status: 'TRIAL',
        startDate: new Date().toISOString(),
        endDate: new Date(Date.now() + 14 * 86400000).toISOString(),
        autoRenew: true,
        isActive: true,
      });
    }
  };

  useEffect(() => {
    loadSubscription();
  }, []);

  const handleUpgrade = async (plan: SubscriptionPlan) => {
    setLoadingPlan(plan);
    try {
      const updated = await subscriptionsApi.upgrade(plan, `PAY-REF-${Date.now()}`);
      setCurrentSub(updated);
    } catch (err) {
      alert('Upgrade failed. Please check backend connection.');
    } finally {
      setLoadingPlan(null);
    }
  };

  const plans = [
    {
      title: '14-Day Free Trial',
      plan: 'TRIAL' as SubscriptionPlan,
      price: '$0',
      period: '14 days',
      features: ['Up to 500 messages', 'Basic keyword matching', 'Single reply automation'],
    },
    {
      title: 'Monthly SaaS',
      plan: 'MONTHLY' as SubscriptionPlan,
      price: '$29',
      period: 'per month',
      popular: true,
      features: ['Unlimited WhatsApp messages', 'All 5 Keyword match modes', 'Multi-reply sequence delays', 'Out-of-office business hours'],
    },
    {
      title: 'Annual Enterprise',
      plan: 'YEARLY' as SubscriptionPlan,
      price: '$290',
      period: 'per year (2 months free)',
      features: ['All Monthly features', 'Priority queue retry engine', 'Dedicated webhook instance', 'Unlimited CRM contacts'],
    },
    {
      title: 'Lifetime Pass',
      plan: 'LIFETIME' as SubscriptionPlan,
      price: '$999',
      period: 'one-time payment',
      features: ['Lifetime access with no renewal', 'Full source code access', 'All future SaaS updates included'],
    },
  ];

  return (
    <Box>
      <Box mb={3}>
        <Typography variant="h4" fontWeight={700}>
          Subscription & Billing Plans
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Manage SaaS subscription tiers, upgrades, and active account access
        </Typography>
      </Box>

      {isUpgradeRequired && (
        <Alert severity="error" sx={{ mb: 3 }}>
          Your subscription is currently EXPIRED or SUSPENDED. Please upgrade to continue using protected WhatsApp automation APIs.
        </Alert>
      )}

      {currentSub && (
        <Card sx={{ mb: 4, bgcolor: 'primary.main', color: 'white' }}>
          <CardContent>
            <Box display="flex" justifyContent="space-between" alignItems="center" flexWrap="wrap">
              <Box>
                <Typography variant="overline" sx={{ opacity: 0.8 }}>
                  CURRENT ACTIVE PLAN
                </Typography>
                <Typography variant="h4" fontWeight={700}>
                  {currentSub.planType} PLAN
                </Typography>
                <Typography variant="body2" sx={{ opacity: 0.9, mt: 0.5 }}>
                  Valid until: {new Date(currentSub.endDate).toLocaleDateString()}
                </Typography>
              </Box>
              <StatusChip status={currentSub.status} size="medium" />
            </Box>
          </CardContent>
        </Card>
      )}

      {/* Plan Tier Cards */}
      <Grid container spacing={3}>
        {plans.map((p) => {
          const isCurrent = currentSub?.planType === p.plan;
          return (
            <Grid size={{ xs: 12, sm: 6, md: 3 }} key={p.plan}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  position: 'relative',
                  border: p.popular ? '2px solid' : '1px solid',
                  borderColor: p.popular ? 'primary.main' : 'divider',
                }}
              >
                {p.popular && (
                  <Chip
                    label="MOST POPULAR"
                    color="primary"
                    size="small"
                    icon={<StarIcon />}
                    sx={{ position: 'absolute', top: 12, right: 12, fontWeight: 700 }}
                  />
                )}

                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" fontWeight={700}>
                    {p.title}
                  </Typography>
                  <Box my={2}>
                    <Typography variant="h3" fontWeight={700} display="inline">
                      {p.price}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" display="inline" sx={{ ml: 1 }}>
                      /{p.period}
                    </Typography>
                  </Box>

                  <List>
                    {p.features.map((feat) => (
                      <ListItem key={feat} disableGutters sx={{ py: 0.5 }}>
                        <ListItemIcon sx={{ minWidth: 30, color: 'success.main' }}>
                          <CheckCircleIcon fontSize="small" />
                        </ListItemIcon>
                        <ListItemText primary={feat} slotProps={{ primary: { fontSize: '0.85rem' } }} />
                      </ListItem>
                    ))}
                  </List>
                </CardContent>

                <Box p={2}>
                  <Button
                    variant={isCurrent ? 'outlined' : p.popular ? 'contained' : 'outlined'}
                    color="primary"
                    fullWidth
                    disabled={isCurrent || loadingPlan === p.plan}
                    onClick={() => handleUpgrade(p.plan)}
                  >
                    {isCurrent ? 'Current Plan' : `Upgrade to ${p.plan}`}
                  </Button>
                </Box>
              </Card>
            </Grid>
          );
        })}
      </Grid>
    </Box>
  );
};
