import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

export default function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await login(username, password)
      const rol = JSON.parse(localStorage.getItem('user')).rol
      const path = `/${rol.toLowerCase()}`
      navigate(path, { replace: true })
    } catch (err) {
      console.error('>>> [LOGIN-FRONTEND] Error en login:', err)
      if (err.response) {
        console.error('>>> [LOGIN-FRONTEND] Status:', err.response.status, 'Data:', err.response.data)
      } else if (err.request) {
        console.error('>>> [LOGIN-FRONTEND] No se recibio respuesta del servidor (error de red)')
      } else {
        console.error('>>> [LOGIN-FRONTEND] Error al configurar la peticion:', err.message)
      }
      setError('Usuario o contraseña incorrectos')
    }
  }

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.brand}>Mi Restaurante</h1>
        <p style={styles.subtitle}>Inicia sesión para continuar</p>
        <form onSubmit={handleSubmit} style={styles.form}>
          <input
            placeholder="Usuario"
            value={username}
            onChange={e => setUsername(e.target.value)}
            style={styles.input}
            autoFocus
          />
          <input
            type="password"
            placeholder="Contraseña"
            value={password}
            onChange={e => setPassword(e.target.value)}
            style={styles.input}
          />
          {error && <p style={styles.error}>{error}</p>}
          <button type="submit" style={styles.button}>Entrar</button>
        </form>
      </div>
    </div>
  )
}

const styles = {
  container: {
    minHeight: '100vh', display: 'flex', alignItems: 'center',
    justifyContent: 'center', background: '#f5f5f5', padding: 20,
  },
  card: {
    background: '#fff', borderRadius: 12, padding: '40px 32px',
    width: '100%', maxWidth: 360, boxShadow: '0 2px 12px rgba(0,0,0,0.08)',
    textAlign: 'center',
  },
  brand: { fontSize: 28, color: '#d32f2f', margin: '0 0 4px' },
  subtitle: { fontSize: 14, color: '#888', margin: '0 0 24px' },
  form: { display: 'flex', flexDirection: 'column', gap: 12 },
  input: {
    padding: '12px 14px', borderRadius: 8, border: '1px solid #ddd',
    fontSize: 15, outline: 'none',
  },
  button: {
    padding: '12px', borderRadius: 8, border: 'none', background: '#d32f2f',
    color: '#fff', fontSize: 16, fontWeight: 600, cursor: 'pointer',
    marginTop: 4,
  },
  error: { color: '#d32f2f', fontSize: 13, margin: 0 },
}
