"use client"

import { useState } from "react"
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Checkbox,
  TextField,
  Box,
  Typography,
  Divider,
  Chip,
  Fade,
} from "@mui/material"
import { Delete, Edit, Add, Save, Cancel, CheckCircle, Schedule } from "@mui/icons-material"
import type { TodoList, Task } from "../types"
import {createTask, updateTask, deleteTask} from "../services/api" // Assurez-vous que le chemin d'importation est correct

interface TaskDialogProps {
  open: boolean
  onClose: () => void
  list: TodoList
  onUpdateList: (list: TodoList) => void
}

export default function TaskDialog({ open, onClose, list, onUpdateList }: TaskDialogProps) {
  const [newTaskText, setNewTaskText] = useState("")
  const [editingTaskId, setEditingTaskId] = useState<string | null>(null)
  const [editingText, setEditingText] = useState("")

  const handleAddTask = async () => {
    if (newTaskText.trim()) {
      try {
        const newTask = await createTask(list.id, newTaskText.trim())
        const updatedList = { ...list, tasks: [...list.tasks, newTask] }
        onUpdateList(updatedList)
        setNewTaskText("")
      } catch (error) {
        // Optionnel : afficher une erreur
      }
    }
  }

  const handleToggleTask = async (taskId: string) => {
    const task = list.tasks.find((t) => t.id === taskId)
    if (task) {
      try {
        const updatedTask = await updateTask(list.id, { ...task, done: !task.done })
        const updatedList = {
          ...list,
          tasks: list.tasks.map((t) => (t.id === taskId ? updatedTask : t)),
        }
        onUpdateList(updatedList)
      } catch (error) {
        // Optionnel : afficher une erreur
      }
    }
  }

  const handleDeleteTask = async (taskId: string) => {
    try {
      await deleteTask(list.id, taskId)
      const updatedList = {
        ...list,
        tasks: list.tasks.filter((task) => task.id !== taskId),
      }
      onUpdateList(updatedList)
    } catch (error) {
      // Optionnel : afficher une erreur
    }
  }

  const handleStartEdit = (task: Task) => {
    setEditingTaskId(task.id)
    setEditingText(task.text)
  }

  const handleSaveEdit = async () => {
    if (editingText.trim() && editingTaskId) {
      const task = list.tasks.find((t) => t.id === editingTaskId)
      if (task) {
        try {
          const updatedTask = await updateTask(list.id, { ...task, text: editingText.trim() })
          const updatedList = {
            ...list,
            tasks: list.tasks.map((t) => (t.id === editingTaskId ? updatedTask : t)),
          }
          onUpdateList(updatedList)
          setEditingTaskId(null)
          setEditingText("")
        } catch (error) {
          // Optionnel : afficher une erreur
        }
      }
    }
  }

  const handleCancelEdit = () => {
    setEditingTaskId(null)
    setEditingText("")
  }

  const completedTasks = list.tasks.filter((task) => task.done)
  const pendingTasks = list.tasks.filter((task) => !task.done)

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: { borderRadius: 3, maxHeight: "90vh" },
      }}
    >
      <DialogTitle sx={{ pb: 2 }}>
        <Box sx={{ display: "flex", alignItems: "center", gap: 2, mb: 1 }}>
          <Typography variant="h6" component="div" sx={{ fontWeight: 600 }}>
            {list.title}
          </Typography>
          {completedTasks.length === list.tasks.length && list.tasks.length > 0 && (
            <Fade in>
              <Chip icon={<CheckCircle />} label="Terminé !" color="success" size="small" />
            </Fade>
          )}
        </Box>
        <Box sx={{ display: "flex", gap: 2 }}>
          <Chip
            icon={<CheckCircle />}
            label={`${completedTasks.length} terminée${completedTasks.length !== 1 ? "s" : ""}`}
            color="success"
            variant="outlined"
            size="small"
          />
          <Chip
            icon={<Schedule />}
            label={`${pendingTasks.length} en cours`}
            color="warning"
            variant="outlined"
            size="small"
          />
        </Box>
      </DialogTitle>

      <DialogContent sx={{ p: 0 }}>
        <Box sx={{ p: 3, borderBottom: 1, borderColor: "divider" }}>
          <Box sx={{ display: "flex", gap: 2 }}>
            <TextField
              fullWidth
              placeholder="Ajouter une nouvelle tâche..."
              value={newTaskText}
              onChange={(e) => setNewTaskText(e.target.value)}
              onKeyPress={(e) => e.key === "Enter" && handleAddTask()}
              variant="outlined"
              size="small"
              sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
            />
            <Button
              variant="contained"
              onClick={handleAddTask}
              disabled={!newTaskText.trim()}
              startIcon={<Add />}
              sx={{ borderRadius: 2, minWidth: 120 }}
            >
              Ajouter
            </Button>
          </Box>
        </Box>

        <List sx={{ maxHeight: 400, overflow: "auto" }}>
          {pendingTasks.length > 0 && (
            <>
              <ListItem sx={{ py: 1 }}>
                <Typography variant="subtitle2" color="text.secondary" sx={{ fontWeight: 600 }}>
                  À faire ({pendingTasks.length})
                </Typography>
              </ListItem>
              {pendingTasks.map((task) => (
                <ListItem key={task.id} dense sx={{ py: 1 }}>
                  <Checkbox checked={task.done} onChange={() => handleToggleTask(task.id)} color="primary" />
                  {editingTaskId === task.id ? (
                    <Box sx={{ display: "flex", alignItems: "center", gap: 1, flex: 1 }}>
                      <TextField
                        fullWidth
                        value={editingText}
                        onChange={(e) => setEditingText(e.target.value)}
                        onKeyPress={(e) => e.key === "Enter" && handleSaveEdit()}
                        size="small"
                        autoFocus
                      />
                      <IconButton size="small" onClick={handleSaveEdit} color="primary">
                        <Save />
                      </IconButton>
                      <IconButton size="small" onClick={handleCancelEdit}>
                        <Cancel />
                      </IconButton>
                    </Box>
                  ) : (
                    <>
                      <ListItemText primary={task.text} sx={{ "& .MuiListItemText-primary": { fontWeight: 500 } }} />
                      <ListItemSecondaryAction>
                        <IconButton size="small" onClick={() => handleStartEdit(task)} sx={{ mr: 1 }}>
                          <Edit />
                        </IconButton>
                        <IconButton size="small" onClick={() => handleDeleteTask(task.id)} color="error">
                          <Delete />
                        </IconButton>
                      </ListItemSecondaryAction>
                    </>
                  )}
                </ListItem>
              ))}
            </>
          )}

          {completedTasks.length > 0 && (
            <>
              {pendingTasks.length > 0 && <Divider />}
              <ListItem sx={{ py: 1 }}>
                <Typography variant="subtitle2" color="text.secondary" sx={{ fontWeight: 600 }}>
                  Terminées ({completedTasks.length})
                </Typography>
              </ListItem>
              {completedTasks.map((task) => (
                <ListItem key={task.id} dense sx={{ py: 1 }}>
                  <Checkbox checked={task.done} onChange={() => handleToggleTask(task.id)} color="primary" />
                  <ListItemText
                    primary={task.text}
                    sx={{
                      "& .MuiListItemText-primary": {
                        textDecoration: "line-through",
                        color: "text.secondary",
                        fontWeight: 400,
                      },
                    }}
                  />
                  <ListItemSecondaryAction>
                    <IconButton size="small" onClick={() => handleDeleteTask(task.id)} color="error">
                      <Delete />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </>
          )}

          {list.tasks.length === 0 && (
            <Box sx={{ textAlign: "center", py: 6, color: "text.secondary" }}>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 500 }}>
                🎯 Prêt à commencer ?
              </Typography>
              <Typography variant="body2">Ajoutez votre première tâche pour organiser cette liste</Typography>
            </Box>
          )}
        </List>
      </DialogContent>

      <DialogActions sx={{ p: 3 }}>
        <Button onClick={onClose} variant="outlined" sx={{ borderRadius: 2 }}>
          Fermer
        </Button>
      </DialogActions>
    </Dialog>
  )
}
