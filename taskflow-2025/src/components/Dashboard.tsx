"use client"

import React, { useState } from "react"
import {
  Box,
  AppBar,
  Toolbar,
  Typography,
  Button,
  Container,
  Grid,
  Fab,
  Avatar,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
  Chip,
  Fade,
  IconButton,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem as SelectMenuItem,
  Paper,
} from "@mui/material"
import { Add, Logout, Person, TrendingUp, Brightness4, Brightness7 } from "@mui/icons-material";
import * as api from "../services/api";
import type { User, TodoList } from "../types";
import ListCard from "./ListCard";
import CreateListDialog from "./CreateListDialog"
import TaskDialog from "./TaskDialog"

interface DashboardProps {
  user: User;
  lists: TodoList[];
  onListsChanged: () => void; // Changed from onUpdateLists
  onLogout: () => void;
  darkMode: boolean;
  toggleTheme: () => void;
}

export default function Dashboard({ user, lists, onListsChanged, onLogout, darkMode, toggleTheme }: DashboardProps) {
  const [createListOpen, setCreateListOpen] = useState(false);
  const [taskDialogOpen, setTaskDialogOpen] = useState(false);
  const [selectedListId, setSelectedListId] = useState<string | null>(null)
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null)

  // États pour la recherche et le tri
  const [searchTerm, setSearchTerm] = useState("")
  const [sortCriteria, setSortCriteria] = useState<"date" | "title">("date");

  const handleCreateList = async (title: string) => {
    try {
      await api.createList(title);
      onListsChanged(); // Notify App.tsx to refresh lists
    } catch (error) {
      console.error("Failed to create list:", error);
      // Optionally: show an error message to the user
    }
  };

  const handleDeleteList = async (listId: string) => {
    try {
      await api.deleteList(listId);
      onListsChanged(); // Notify App.tsx to refresh lists
    } catch (error) {
      console.error("Failed to delete list:", error);
      // Optionally: show an error message to the user
    }
  };

  const handleOpenTasks = (listId: string) => {
    setSelectedListId(listId);
    setTaskDialogOpen(true);
  };

  const handleUpdateList = async (updatedList: TodoList) => {
    try {
      await api.updateList(updatedList);
      onListsChanged(); // Notify App.tsx to refresh lists
    } catch (error) {
      console.error("Failed to update list:", error);
      // Optionally: show an error message to the user
    }
  };

  const handleMenuClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget)
  }

  const handleMenuClose = () => {
    setAnchorEl(null)
  }

  // Filtrer et trier les listes
  const filteredLists = lists.filter((list) =>
    list.title.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const sortedLists = [...filteredLists].sort((a, b) => {
    if (sortCriteria === "title") {
      return a.title.localeCompare(b.title)
    }
    // Tri par date décroissante
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  })

  const completedTasks = lists.reduce((total, list) => total + list.tasks.filter((task) => task.done).length, 0)
  const totalTasks = lists.reduce((total, list) => total + list.tasks.length, 0)
  const completionRate = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0

  return (
    <Box>
      <AppBar
        position="static"
        elevation={0}
        sx={{
          bgcolor: "background.paper",
          color: "text.primary",
          borderBottom: 1,
          borderColor: "divider",
        }}
      >
        <Toolbar sx={{ py: 1 }}>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 700 }}>
            TaskFlow 2025
          </Typography>

          <IconButton onClick={toggleTheme} sx={{ mr: 1 }}>
            {darkMode ? <Brightness7 /> : <Brightness4 />}
          </IconButton>

          <Button
            onClick={handleMenuClick}
            startIcon={
              <Avatar sx={{ width: 32, height: 32, bgcolor: "primary.main" }}>
                <Person sx={{ fontSize: 18 }} />
              </Avatar>
            }
            sx={{ textTransform: "none", color: "text.primary", borderRadius: 3 }}
          >
            {user.name}
          </Button>

          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
            transformOrigin={{ horizontal: "right", vertical: "top" }}
            anchorOrigin={{ horizontal: "right", vertical: "bottom" }}
            PaperProps={{
              sx: { borderRadius: 2, mt: 1 },
            }}
          >
            <MenuItem onClick={onLogout}>
              <ListItemIcon>
                <Logout fontSize="small" />
              </ListItemIcon>
              <ListItemText>Déconnexion</ListItemText>
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Fade in timeout={600}>
          <Box sx={{ mb: 4 }}>
            <Typography variant="h4" component="h1" gutterBottom sx={{ fontWeight: 700 }}>
              Bonjour, {user.name} 👋
            </Typography>
            <Box sx={{ display: "flex", gap: 2, flexWrap: "wrap", alignItems: "center" }}>
              <Typography variant="body1" color="text.secondary">
                Vous avez {completedTasks} tâche{completedTasks !== 1 ? "s" : ""} terminée{completedTasks !== 1 ? "s" : ""} sur {totalTasks} au total
              </Typography>
              {totalTasks > 0 && (
                <Chip
                  icon={<TrendingUp />}
                  label={`${completionRate}% complété`}
                  color={completionRate >= 70 ? "success" : completionRate >= 40 ? "warning" : "default"}
                  variant="outlined"
                />
              )}
            </Box>
          </Box>
        </Fade>

        {/* Section stylisée pour la recherche et le tri */}
        <Paper
          elevation={3}
          sx={{
            p: 2,
            display: "flex",
            flexWrap: "wrap",
            alignItems: "center",
            gap: 2,
            borderRadius: 3,
            mb: 4,
            bgcolor: "background.paper",
          }}
        >
          <TextField
            variant="outlined"
            label="Rechercher"
            placeholder="Rechercher une liste..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            size="small"
            sx={{ flexGrow: 1 }}
          />
          <FormControl variant="outlined" size="small" sx={{ minWidth: 150 }}>
            <InputLabel id="sort-label">Trier par</InputLabel>
            <Select
              labelId="sort-label"
              value={sortCriteria}
              onChange={(e) => setSortCriteria(e.target.value as "date" | "title")}
              label="Trier par"
            >
              <SelectMenuItem value="date">Date</SelectMenuItem>
              <SelectMenuItem value="title">Titre</SelectMenuItem>
            </Select>
          </FormControl>
        </Paper>

        <Grid container spacing={3}>
          {sortedLists.map((list, index) => (
            <Grid item xs={12} sm={6} md={4} key={list.id}>
              <Fade in timeout={800 + index * 100}>
                <div>
                  <ListCard
                    list={list}
                    onDelete={() => handleDeleteList(list.id)}
                    onOpenTasks={() => handleOpenTasks(list.id)}
                  />
                </div>
              </Fade>
            </Grid>
          ))}
        </Grid>

        {lists.length === 0 && (
          <Fade in timeout={1000}>
            <Box
              sx={{
                textAlign: "center",
                py: 8,
                color: "text.secondary",
              }}
            >
              <Typography variant="h5" gutterBottom sx={{ fontWeight: 600 }}>
                🚀 Prêt à être productif ?
              </Typography>
              <Typography variant="body1" sx={{ mb: 3 }}>
                Créez votre première liste pour commencer à organiser vos tâches
              </Typography>
              <Button variant="contained" size="large" startIcon={<Add />} onClick={() => setCreateListOpen(true)} sx={{ borderRadius: 3 }}>
                Créer ma première liste
              </Button>
            </Box>
          </Fade>
        )}
      </Container>

      <Fab
        color="primary"
        aria-label="add"
        sx={{
          position: "fixed",
          bottom: 24,
          right: 24,
        }}
        onClick={() => setCreateListOpen(true)}
      >
        <Add />
      </Fab>

      <CreateListDialog
        open={createListOpen}
        onClose={() => setCreateListOpen(false)}
        onCreateList={handleCreateList}
      />

      {sortedLists.length > 0 && selectedListId && (
        <TaskDialog
          open={taskDialogOpen}
          onClose={() => setTaskDialogOpen(false)}
          list={lists.find((list) => list.id === selectedListId)!}
          onUpdateList={handleUpdateList}
        />
      )}
    </Box>
  )
}
