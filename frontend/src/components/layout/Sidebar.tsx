import React from 'react';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
  Box,
  Divider,
  Chip,
} from '@mui/material';
import { useLocation, useNavigate } from 'react-router-dom';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import PersonIcon from '@mui/icons-material/Person';
import VpnKeyIcon from '@mui/icons-material/VpnKey';
import ReplyIcon from '@mui/icons-material/Reply';
import DescriptionIcon from '@mui/icons-material/Description';
import CardMembershipIcon from '@mui/icons-material/CardMembership';
import AnalyticsIcon from '@mui/icons-material/Analytics';
import SecurityIcon from '@mui/icons-material/Security';
import SettingsIcon from '@mui/icons-material/Settings';
import SpeedIcon from '@mui/icons-material/Speed';

interface SidebarProps {
  drawerWidth: number;
  mobileOpen: boolean;
  onDrawerToggle: () => void;
}

const menuItems = [
  { text: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
  { text: 'Users', icon: <PeopleIcon />, path: '/users' },
  { text: 'Customers', icon: <PersonIcon />, path: '/customers' },
  { text: 'Keywords', icon: <VpnKeyIcon />, path: '/keywords' },
  { text: 'Replies', icon: <ReplyIcon />, path: '/replies' },
  { text: 'Templates', icon: <DescriptionIcon />, path: '/templates' },
  { text: 'Subscriptions', icon: <CardMembershipIcon />, path: '/subscriptions' },
  { text: 'Analytics', icon: <AnalyticsIcon />, path: '/analytics' },
  { text: 'Logs', icon: <SecurityIcon />, path: '/logs' },
  { text: 'Settings', icon: <SettingsIcon />, path: '/settings' },
];

export const Sidebar: React.FC<SidebarProps> = ({ drawerWidth, mobileOpen, onDrawerToggle }) => {
  const navigate = useNavigate();
  const location = useLocation();

  const drawerContent = (
    <Box display="flex" flexDirection="column" height="100%">
      {/* Brand Header */}
      <Box p={3} display="flex" alignItems="center" gap={1.5}>
        <Box
          sx={{
            width: 40,
            height: 40,
            borderRadius: 2.5,
            bgcolor: 'primary.main',
            color: 'white',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <SpeedIcon />
        </Box>
        <Box>
          <Typography variant="h6" fontWeight={700} lineHeight={1.2}>
            OrderFlow
          </Typography>
          <Typography variant="caption" color="text.secondary" fontWeight={500}>
            Admin Enterprise
          </Typography>
        </Box>
      </Box>

      <Divider sx={{ opacity: 0.15 }} />

      {/* Navigation List */}
      <List sx={{ px: 2, py: 2, flexGrow: 1 }}>
        {menuItems.map((item) => {
          const isSelected = location.pathname === item.path;
          return (
            <ListItem key={item.text} disablePadding sx={{ mb: 0.5 }}>
              <ListItemButton
                onClick={() => {
                  navigate(item.path);
                  if (mobileOpen) onDrawerToggle();
                }}
                sx={{
                  borderRadius: 2,
                  bgcolor: isSelected ? 'primary.main' : 'transparent',
                  color: isSelected ? 'white' : 'text.primary',
                  '&:hover': {
                    bgcolor: isSelected ? 'primary.dark' : 'action.hover',
                  },
                }}
              >
                <ListItemIcon sx={{ color: isSelected ? 'white' : 'text.secondary', minWidth: 40 }}>
                  {item.icon}
                </ListItemIcon>
                <ListItemText
                  primary={item.text}
                  primaryTypographyProps={{ fontSize: '0.9rem', fontWeight: isSelected ? 600 : 500 }}
                />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>

      <Box p={2} m={2} sx={{ borderRadius: 3, bgcolor: 'action.hover', textAlign: 'center' }}>
        <Typography variant="caption" color="text.secondary" display="block">
          SaaS Engine v1.0.0
        </Typography>
        <Chip label="ONLINE" size="small" color="success" sx={{ mt: 0.5, fontWeight: 700, fontSize: '0.65rem' }} />
      </Box>
    </Box>
  );

  return (
    <Box component="nav" sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}>
      {/* Mobile Drawer */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={onDrawerToggle}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', sm: 'none' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
        }}
      >
        {drawerContent}
      </Drawer>

      {/* Desktop Permanent Drawer */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', sm: 'block' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
        }}
        open
      >
        {drawerContent}
      </Drawer>
    </Box>
  );
};
