"use client"

import { useState, useEffect } from "react";
import * as api from "./services/api";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";
import { Box, Typography, Button } from "@mui/material";
import AuthPage from "./components/AuthPage";
import Dashboard from "./components/Dashboard";
import type { User, TodoList } from "./types"

function App() {
  const [user, setUser] = useState<User | null>(null);
  const [lists, setLists] = useState<TodoList[]>([]);
  const [darkMode, setDarkMode] = useState(false);
  const [isLoadingLists, setIsLoadingLists] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const savedUser = localStorage.getItem("taskflow_user");
    if (savedUser) {
      setUser(JSON.parse(savedUser));
    }

    setIsLoadingLists(true);
    setError(null); // Clear previous errors
    api.getLists()
      .then(fetchedLists => {
        setLists(fetchedLists);
      })
      .catch(fetchError => {
        console.error("Failed to fetch lists:", fetchError);
        setError("Failed to load lists. Please try again later.");
      })
      .finally(() => {
        setIsLoadingLists(false);
      });
  }, []);

  const handleLogin = (userData: User) => {
    setUser(userData);
    localStorage.setItem("taskflow_user", JSON.stringify(userData));

    setIsLoadingLists(true);
    setError(null); // Clear previous errors
    api.getLists()
      .then(fetchedLists => {
        setLists(fetchedLists);
      })
      .catch(fetchError => {
        console.error("Failed to fetch lists for user:", fetchError);
        setError("Failed to load your data after login. Please try again.");
      })
      .finally(() => {
        setIsLoadingLists(false);
      });
  };

  const handleLogout = () => {
    setUser(null);
    setLists([]); // Clear lists in state
    localStorage.removeItem("taskflow_user");
    // No need to call localStorage.removeItem("taskflow_lists") directly here anymore,
    // as list operations should go through api.ts.
    // If api.ts needs explicit clearing, a dedicated function like api.clearUserLists() could be added.
    // For now, the next login will call api.getLists().
  };

  const handleListsUpdated = () => {
    setIsLoadingLists(true);
    setError(null); // Clear previous errors
    api.getLists()
      .then(fetchedLists => {
        setLists(fetchedLists);
      })
      .catch(fetchError => {
        console.error("Failed to fetch lists after update:", fetchError);
        setError("Failed to update lists. Please check your connection or try again.");
      })
      .finally(() => {
        setIsLoadingLists(false);
      });
  };

  const toggleTheme = () => {
    setDarkMode((prev) => !prev)
  }

  const theme = createTheme({
    palette: {
      mode: darkMode ? "dark" : "light",
      primary: {
        main: "#1976d2",
        light: "#42a5f5",
        dark: "#1565c0",
      },
      secondary: {
        main: "#9c27b0",
        light: "#ba68c8",
        dark: "#7b1fa2",
      },
      background: {
        default: darkMode ? "#121212" : "#fafafa",
        paper: darkMode ? "#1e1e1e" : "#ffffff",
      },
      success: {
        main: "#2e7d32",
        light: "#4caf50",
        dark: "#1b5e20",
      },
    },
    typography: {
      fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
      h4: {
        fontWeight: 600,
        letterSpacing: "-0.02em",
      },
      h6: {
        fontWeight: 600,
        letterSpacing: "-0.01em",
      },
      button: {
        fontWeight: 500,
        letterSpacing: "0.02em",
      },
    },
    shape: {
      borderRadius: 16,
    },
    components: {
      MuiCard: {
        styleOverrides: {
          root: {
            boxShadow: "0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24)",
            transition: "all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1)",
            "&:hover": {
              boxShadow: "0 14px 28px rgba(0,0,0,0.25), 0 10px 10px rgba(0,0,0,0.22)",
              transform: "translateY(-2px)",
            },
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: {
            textTransform: "none",
            fontWeight: 500,
            borderRadius: 12,
            padding: "10px 24px",
          },
          contained: {
            boxShadow: "0 2px 8px rgba(25, 118, 210, 0.3)",
            "&:hover": {
              boxShadow: "0 4px 16px rgba(25, 118, 210, 0.4)",
            },
          },
        },
      },
      MuiFab: {
        styleOverrides: {
          root: {
            boxShadow: "0 4px 16px rgba(25, 118, 210, 0.3)",
            "&:hover": {
              boxShadow: "0 6px 20px rgba(25, 118, 210, 0.4)",
            },
          },
        },
      },
    },
  })

  })

  if (error) {
    return (
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Box sx={{ minHeight: "100vh", bgcolor: "background.default", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", p: 3 }}>
          <Typography variant="h5" color="error" gutterBottom>
            An Error Occurred
          </Typography>
          <Typography color="text.secondary">{error}</Typography>
          <Button variant="contained" onClick={() => {
            setError(null);
            if (user) {
              handleListsUpdated();
            } else {
              // For initial load error before user is set, re-trigger useEffect logic
              // This is a simplified approach; ideally, useEffect would have its own retry.
              // Or simply reload:
              window.location.reload();
            }
          }} sx={{mt: 2}}>
            Try Again
          </Button>
        </Box>
      </ThemeProvider>
    );
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
        {!user ? (
          <AuthPage onLogin={handleLogin} />
        ) : (
          <Dashboard
            user={user}
            lists={lists}
            onListsChanged={handleListsUpdated}
            onLogout={handleLogout}
            darkMode={darkMode}
            toggleTheme={toggleTheme}
            isLoadingLists={isLoadingLists} // Pass the loading state
          />
        )}
      </Box>
    </ThemeProvider>
  )
}

export default App
