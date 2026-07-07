import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { mesas } from '../../api/client'
import Header from '../../components/Header'

export default function ListaMesasCajero() {
  const [lista, setLista] = useState([])
  const navigate = useNavigate()

  useEffect(() => {
    mesas.list().then(setLista)
  }, [])

  return (
    <div style={styles.page}>
      <Header />
      <div style={styles.content}>
        <h2 style={styles.title}>Mesas</h2>
        <div style={styles.list}>
          {lista.map(m => (
            <div key={m.id} style={styles.card}
              onClick={() => navigate(`mesa/${m.id}`)}>
              <span style={styles.mesaName}>Mesa {m.numero}</span>
              <div style={{
                ...styles.status,
                background: m.estado === 'OCUPADA' ? '#d32f2f' : '#4caf50'
              }} />
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

const styles = {
  page: { minHeight: '100vh', background: '#f5f5f5' },
  content: { padding: '16px 16px 100px' },
  title: { fontSize: 20, fontWeight: 700, marginBottom: 12, color: '#333' },
  list: { display: 'flex', flexDirection: 'column', gap: 10 },
  card: {
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    background: '#fff', borderRadius: 10, padding: '16px 20px',
    cursor: 'pointer', boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
    fontSize: 20, fontWeight: 600,
  },
  mesaName: { color: '#333' },
  status: { width: 28, height: 28, borderRadius: '50%', flexShrink: 0 },
}
