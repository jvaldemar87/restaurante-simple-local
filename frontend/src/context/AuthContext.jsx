import { createContext, useContext, useState, useEffect } from 'react'
import { auth } from '../api/client'
import api from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) {
      setLoading(false)
      return
    }
    api.get('/auth/me')
      .then(() => {
        const stored = localStorage.getItem('user')
        if (stored) {
          setUser(JSON.parse(stored))
        }
      })
      .catch(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
      })
      .finally(() => setLoading(false))
  }, [])

  const login = async (username, password) => {
    const data = await auth.login(username, password)
    const userData = { token: data.token, rol: data.rol, nombre: data.nombre }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(userData))
    setUser(userData)
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
  }

  const isAuthenticated = !!user
  const isAdmin = user?.rol === 'ADMIN'
  const isMesero = user?.rol === 'MESERO'
  const isCajero = user?.rol === 'CAJERO'

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated, isAdmin, isMesero, isCajero, loading }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
