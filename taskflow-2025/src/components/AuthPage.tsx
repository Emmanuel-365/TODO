"use client"

import type React from "react"

import { useState } from "react"
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Tab,
  Tabs,
  Container,
  Avatar,
  Alert,
  Fade,
} from "@mui/material"
import { CheckCircle, Rocket } from "@mui/icons-material"
import type { User } from "../types"
import { register, login } from "../services/api"

interface AuthPageProps {
  onLogin: (user: User) => void
}

export default function AuthPage({ onLogin }: AuthPageProps) {
  const [activeTab, setActiveTab] = useState(0)
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    name: "",
  })
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue)
    setError("")
  }

  const handleInputChange = (field: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({
      ...prev,
      [field]: event.target.value,
    }))
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError("")
    setLoading(true)
    try {
      if (!formData.email || !formData.password) {
        setError("Veuillez remplir tous les champs requis")
        setLoading(false)
        return
      }
      if (activeTab === 1 && !formData.name) {
        setError("Veuillez remplir tous les champs requis")
        setLoading(false)
        return
      }
      if (activeTab === 1) {
        // Inscription
        await register(formData.email, formData.name, formData.password)
        // Optionnel : afficher un message de succès ou basculer sur l'onglet connexion
        setActiveTab(0)
        setLoading(false)
        return
      } else {
        // Connexion
        const auth = await login(formData.email, formData.password)
        // Ici, tu peux stocker le token dans le localStorage ou le state global
        // et récupérer les infos utilisateur si besoin
        const user: User = {
          id: "", // À remplacer par l'id réel si le backend le retourne
          email: formData.email,
          name: formData.email.split("@")[0],
        }
        setLoading(false)
        onLogin(user)
        return
      }
    } catch (err: any) {
      setError(err.message || "Erreur réseau")
      setLoading(false)
    }
  }

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: "100vh",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          py: 4,
        }}
      >
        <Fade in timeout={800}>
          <Card sx={{ width: "100%", maxWidth: 420 }}>
            <CardContent sx={{ p: 4 }}>
              <Box sx={{ textAlign: "center", mb: 4 }}>
                <Avatar
                  sx={{
                    mx: "auto",
                    mb: 2,
                    bgcolor: "primary.main",
                    width: 64,
                    height: 64,
                  }}
                >
                  <Rocket sx={{ fontSize: 32 }} />
                </Avatar>
                <Typography variant="h4" component="h1" gutterBottom sx={{ fontWeight: 700 }}>
                  TaskFlow 2025
                </Typography>
                <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
                  Gérez vos tâches avec style et efficacité
                </Typography>
                <Box sx={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 1 }}>
                  <CheckCircle sx={{ fontSize: 16, color: "success.main" }} />
                  <Typography variant="body2" color="success.main" sx={{ fontWeight: 500 }}>
                    100% gratuit et sécurisé
                  </Typography>
                </Box>
              </Box>

              <Tabs
                value={activeTab}
                onChange={handleTabChange}
                variant="fullWidth"
                sx={{
                  mb: 3,
                  "& .MuiTab-root": {
                    fontWeight: 500,
                    fontSize: "0.95rem",
                  },
                }}
              >
                <Tab label="Connexion" />
                <Tab label="Inscription" />
              </Tabs>

              {error && (
                <Alert severity="error" sx={{ mb: 2 }}>
                  {error}
                </Alert>
              )}

              <form onSubmit={handleSubmit}>
                <Box sx={{ display: "flex", flexDirection: "column", gap: 2.5 }}>
                  {activeTab === 1 && (
                    <TextField
                      fullWidth
                      label="Nom complet"
                      variant="outlined"
                      value={formData.name}
                      onChange={handleInputChange("name")}
                      required
                    />
                  )}

                  <TextField
                    fullWidth
                    label="Email"
                    type="email"
                    variant="outlined"
                    value={formData.email}
                    onChange={handleInputChange("email")}
                    required
                  />

                  <TextField
                    fullWidth
                    label="Mot de passe"
                    type="password"
                    variant="outlined"
                    value={formData.password}
                    onChange={handleInputChange("password")}
                    required
                  />

                  <Button
                    type="submit"
                    fullWidth
                    variant="contained"
                    size="large"
                    disabled={loading}
                    sx={{ mt: 1, py: 1.5, fontSize: "1rem" }}
                  >
                    {loading ? "Connexion..." : activeTab === 0 ? "Se connecter" : "S'inscrire"}
                  </Button>
                </Box>
              </form>

              <Typography variant="body2" color="text.secondary" sx={{ mt: 3, textAlign: "center" }}>
                🔒 Mode démo - Aucune donnée n'est envoyée sur un serveur
              </Typography>
            </CardContent>
          </Card>
        </Fade>
      </Box>
    </Container>
  )
}
