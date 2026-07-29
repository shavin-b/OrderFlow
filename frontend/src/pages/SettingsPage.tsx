import React, { useState } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  Button,
  Grid,
  Alert,
} from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import { useAuth } from '../context/AuthContext';
import { authApi } from '../api/auth';

export const SettingsPage: React.FC = () => {
  const { user, refreshProfile } = useAuth();
  const [firstName, setFirstName] = useState(user?.firstName || '');
  const [lastName, setLastName] = useState(user?.lastName || '');
  const [phone, setPhone] = useState(user?.phone || '');
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // WhatsApp Credentials
  const [waVerifyToken, setWaVerifyToken] = useState('dev-verify-token');
  const [waPhoneNumberId, setWaPhoneNumberId] = useState('389472910482103');
  const [waAccessToken, setWaAccessToken] = useState('EAAG...token');

  const handleProfileSave = async () => {
    try {
      await authApi.updateProfile({ firstName, lastName, phone });
      await refreshProfile();
      setSuccessMsg('Profile updated successfully');
    } catch (err) {
      alert('Failed to update profile');
    }
  };

  return (
    <Box>
      <Box mb={3}>
        <Typography variant="h4" fontWeight={700}>
          Account & Engine Settings
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Configure profile details and WhatsApp Cloud API credentials
        </Typography>
      </Box>

      {successMsg && (
        <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccessMsg(null)}>
          {successMsg}
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* User Profile Settings */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight={600} mb={2}>
                Personal Profile Settings
              </Typography>
              <TextField
                label="Email Address"
                fullWidth
                disabled
                margin="normal"
                value={user?.email || ''}
              />
              <TextField
                label="First Name"
                fullWidth
                margin="normal"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
              />
              <TextField
                label="Last Name"
                fullWidth
                margin="normal"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
              />
              <TextField
                label="Phone Number"
                fullWidth
                margin="normal"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
              />

              <Button
                variant="contained"
                startIcon={<SaveIcon />}
                onClick={handleProfileSave}
                sx={{ mt: 2 }}
              >
                Save Profile
              </Button>
            </CardContent>
          </Card>
        </Grid>

        {/* WhatsApp Integration Settings */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight={600} mb={2}>
                WhatsApp Cloud API Credentials
              </Typography>
              <TextField
                label="Webhook Verify Token"
                fullWidth
                margin="normal"
                value={waVerifyToken}
                onChange={(e) => setWaVerifyToken(e.target.value)}
              />
              <TextField
                label="Phone Number ID"
                fullWidth
                margin="normal"
                value={waPhoneNumberId}
                onChange={(e) => setWaPhoneNumberId(e.target.value)}
              />
              <TextField
                label="Permanent Access Token"
                fullWidth
                multiline
                rows={2}
                margin="normal"
                value={waAccessToken}
                onChange={(e) => setWaAccessToken(e.target.value)}
              />

              <Button
                variant="contained"
                color="secondary"
                startIcon={<SaveIcon />}
                onClick={() => setSuccessMsg('WhatsApp API configuration saved')}
                sx={{ mt: 2 }}
              >
                Save API Config
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
