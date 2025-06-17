"use client"

import { Card, CardContent, Typography, Box, IconButton, LinearProgress, Chip, Fade } from "@mui/material"
import { Delete, PlayArrow, CheckCircle } from "@mui/icons-material"
import type { TodoList } from "../types"

interface ListCardProps {
  list: TodoList
  onDelete: () => void
  onOpenTasks: () => void
}

export default function ListCard({ list, onDelete, onOpenTasks }: ListCardProps) {
  const completedTasks = list.tasks.filter((task) => task.done).length
  const totalTasks = list.tasks.length
  const progress = totalTasks > 0 ? (completedTasks / totalTasks) * 100 : 0

  const getProgressColor = () => {
    if (progress === 100) return "success"
    if (progress >= 70) return "info"
    if (progress >= 40) return "warning"
    return "primary"
  }

  return (
    <Card
      sx={{
        cursor: "pointer",
        height: "100%",
        display: "flex",
        flexDirection: "column",
      }}
      onClick={onOpenTasks}
    >
      <CardContent sx={{ flexGrow: 1, p: 3 }}>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", mb: 3 }}>
          <Typography variant="h6" component="h2" sx={{ fontWeight: 600, lineHeight: 1.3 }}>
            {list.title}
          </Typography>
          <IconButton
            size="small"
            onClick={(e) => {
              e.stopPropagation()
              onDelete()
            }}
            sx={{
              color: "text.secondary",
              "&:hover": { color: "error.main" },
            }}
          >
            <Delete fontSize="small" />
          </IconButton>
        </Box>

        <Box sx={{ mb: 3 }}>
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 1.5 }}>
            <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
              Progression
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600 }}>
              {completedTasks}/{totalTasks}
            </Typography>
          </Box>
          <LinearProgress
            variant="determinate"
            value={progress}
            color={getProgressColor()}
            sx={{
              height: 8,
              borderRadius: 4,
              bgcolor: "grey.200",
              "& .MuiLinearProgress-bar": {
                borderRadius: 4,
              },
            }}
          />
        </Box>

        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Chip
            label={`${totalTasks} tâche${totalTasks !== 1 ? "s" : ""}`}
            size="small"
            variant="outlined"
            sx={{ fontWeight: 500 }}
          />
          {progress === 100 && totalTasks > 0 ? (
            <Fade in>
              <CheckCircle color="success" />
            </Fade>
          ) : (
            <IconButton size="small" color="primary">
              <PlayArrow />
            </IconButton>
          )}
        </Box>
      </CardContent>
    </Card>
  )
}
