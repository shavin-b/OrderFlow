import React, { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  IconButton,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { DataTable, Column } from '../components/common/DataTable';
import { StatusChip } from '../components/common/StatusChip';
import { AutomationRule, MatchType } from '../types';
import { automationApi } from '../api/automation';

export const KeywordsPage: React.FC = () => {
  const [rules, setRules] = useState<AutomationRule[]>([]);
  const [openModal, setOpenModal] = useState(false);
  const [editingRule, setEditingRule] = useState<Partial<AutomationRule>>({
    name: '',
    priority: 0,
    active: true,
    cooldownSeconds: 0,
    keywords: [{ pattern: '', matchType: 'CONTAINS', ignoreCase: true }],
    replies: [{ messageBody: '', replyOrder: 0, delaySeconds: 0 }],
  });

  const loadRules = async () => {
    try {
      const data = await automationApi.getRules();
      setRules(data);
    } catch (err) {
      setRules([
        {
          id: 1,
          name: 'Order Status Inquiry',
          priority: 10,
          active: true,
          cooldownSeconds: 60,
          triggerCount: 450,
          keywords: [{ pattern: 'order status', matchType: 'CONTAINS', ignoreCase: true }],
          replies: [{ messageBody: 'Please provide your 6-digit Order ID.', replyOrder: 0, delaySeconds: 0 }],
        },
      ]);
    }
  };

  useEffect(() => {
    loadRules();
  }, []);

  const handleToggleActive = async (id: number, current: boolean) => {
    await automationApi.toggleRuleActive(id, !current);
    loadRules();
  };

  const handleSave = async () => {
    if (!editingRule.name) return;
    if (editingRule.id) {
      await automationApi.updateRule(editingRule.id, editingRule);
    } else {
      await automationApi.createRule(editingRule);
    }
    setOpenModal(false);
    loadRules();
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Delete automation rule?')) {
      await automationApi.deleteRule(id);
      loadRules();
    }
  };

  const columns: Column<AutomationRule>[] = [
    { id: 'id', label: 'ID', minWidth: 60 },
    { id: 'name', label: 'Rule Name' },
    {
      id: 'keywords',
      label: 'Trigger Keywords',
      format: (kws) => kws?.map((k: any) => k.pattern).join(', ') || 'N/A',
    },
    {
      id: 'keywords',
      label: 'Match Type',
      format: (kws) => <StatusChip status={kws?.[0]?.matchType || 'CONTAINS'} />,
    },
    { id: 'priority', label: 'Priority', align: 'center' },
    { id: 'triggerCount', label: 'Total Triggers', align: 'center' },
    {
      id: 'active',
      label: 'Active',
      format: (active, row) => (
        <Switch
          checked={!!active}
          onChange={() => row.id && handleToggleActive(row.id, !!active)}
          color="primary"
          size="small"
        />
      ),
    },
    {
      id: 'actions',
      label: 'Actions',
      align: 'right',
      format: (_, row) => (
        <Box display="flex" justifyContent="flex-end" gap={1}>
          <IconButton size="small" onClick={() => { setEditingRule(row); setOpenModal(true); }}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton size="small" color="error" onClick={() => row.id && handleDelete(row.id)}>
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box mb={3}>
        <Typography variant="h4" fontWeight={700}>
          Keywords & Automation Rules
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Configure rule matching priority, keyword patterns, and match types
        </Typography>
      </Box>

      <DataTable
        columns={columns}
        data={rules}
        onAdd={() => {
          setEditingRule({
            name: '',
            priority: 0,
            active: true,
            cooldownSeconds: 0,
            keywords: [{ pattern: '', matchType: 'CONTAINS', ignoreCase: true }],
            replies: [{ messageBody: '', replyOrder: 0, delaySeconds: 0 }],
          });
          setOpenModal(true);
        }}
        addLabel="Create Rule"
      />

      {/* Add / Edit Rule Modal */}
      <Dialog open={openModal} onClose={() => setOpenModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingRule.id ? 'Edit Automation Rule' : 'Create Automation Rule'}</DialogTitle>
        <DialogContent>
          <TextField
            label="Rule Name"
            fullWidth
            margin="dense"
            value={editingRule.name || ''}
            onChange={(e) => setEditingRule((prev) => ({ ...prev, name: e.target.value }))}
          />
          <TextField
            label="Keyword Pattern"
            fullWidth
            margin="dense"
            value={editingRule.keywords?.[0]?.pattern || ''}
            onChange={(e) =>
              setEditingRule((prev) => ({
                ...prev,
                keywords: [{ ...prev.keywords![0], pattern: e.target.value }],
              }))
            }
          />
          <FormControl fullWidth margin="dense">
            <InputLabel>Match Type</InputLabel>
            <Select
              value={editingRule.keywords?.[0]?.matchType || 'CONTAINS'}
              label="Match Type"
              onChange={(e) =>
                setEditingRule((prev) => ({
                  ...prev,
                  keywords: [{ ...prev.keywords![0], matchType: e.target.value as MatchType }],
                }))
              }
            >
              <MenuItem value="CONTAINS">CONTAINS</MenuItem>
              <MenuItem value="EXACT">EXACT</MenuItem>
              <MenuItem value="STARTS_WITH">STARTS_WITH</MenuItem>
              <MenuItem value="ENDS_WITH">ENDS_WITH</MenuItem>
              <MenuItem value="REGEX">REGEX</MenuItem>
            </Select>
          </FormControl>
          <TextField
            label="Automated Reply Message"
            fullWidth
            multiline
            rows={3}
            margin="dense"
            value={editingRule.replies?.[0]?.messageBody || ''}
            onChange={(e) =>
              setEditingRule((prev) => ({
                ...prev,
                replies: [{ ...prev.replies![0], messageBody: e.target.value }],
              }))
            }
          />
          <Box display="flex" gap={2} mt={1}>
            <TextField
              label="Priority (Higher = Evaluated First)"
              type="number"
              fullWidth
              value={editingRule.priority || 0}
              onChange={(e) => setEditingRule((prev) => ({ ...prev, priority: parseInt(e.target.value) }))}
            />
            <TextField
              label="Cooldown (Seconds)"
              type="number"
              fullWidth
              value={editingRule.cooldownSeconds || 0}
              onChange={(e) => setEditingRule((prev) => ({ ...prev, cooldownSeconds: parseInt(e.target.value) }))}
            />
          </Box>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setOpenModal(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSave}>
            Save Rule
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
