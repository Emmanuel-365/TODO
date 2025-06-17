"use client"

import type React from "react"

import { useState } from "react"
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField, Button, Typography } from "@mui/material"
import { Add } from "@mui/icons-material"

interface CreateListDialogProps {
  open: boolean
  onClose: () => void
  onCreateList: (title: string) => void
}

export default function CreateListDialog({ open, onClose, onCreateList }: CreateListDialogProps) {
  const [title, setTitle] = useState("")

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    if (title.trim()) {
      onCreateList(title.trim())
      setTitle("")
      onClose()
    }
  }

  const handleClose = () => {
    setTitle("")
    onClose()
  }

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: { borderRadius: 3 },
      }}
    >
      <form onSubmit={handleSubmit}>
        <DialogTitle sx={{ pb: 1 }}>
          <Typography variant="h6" sx={{ fontWeight: 600 }}>
            Créer une nouvelle liste
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Donnez un nom à votre liste pour organiser vos tâches
          </Typography>
        </DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <TextField
            autoFocus
            fullWidth
            label="Nom de la liste"
            variant="outlined"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Ex: Courses, Travail, Personnel..."
            sx={{ mt: 1 }}
          />
        </DialogContent>
        <DialogActions sx={{ p: 3, pt: 2 }}>
          <Button onClick={handleClose} sx={{ borderRadius: 2 }}>
            Annuler
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={!title.trim()}
            startIcon={<Add />}
            sx={{ borderRadius: 2 }}
          >
            Créer
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
