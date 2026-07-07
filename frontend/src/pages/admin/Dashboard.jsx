import { useNavigate } from 'react-router-dom'
import Header from '../../components/Header'

const modules = [
  { label: 'Mesas', path: '/admin/mesas', color: '#d32f2f' },
  { label: 'Insumos', path: '/admin/insumos', color: '#1976d2' },
  { label: 'Pagos', path: '/admin/pagos', color: '#388e3c' },
  { label: 'Reportes', path: '/admin/reportes', color: '#f57c00' },
]

export default function Dashboard() {
  const navigate = useNavigate()

  return (
    <div style={styles.page}>
      <Header />
      <div style={styles.content}>
        <h2 style={styles.title}>Panel de Administración</h2>
        <div style={styles.grid}>
          {modules.map(m => (
            <div key={m.label} style={{ ...styles.card, borderTop: `4px solid ${m.color}` }}
              onClick={() => navigate(m.path)}>
              <span style={styles.cardLabel}>{m.label}</span>
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
  title: { fontSize: 20, fontWeight: 700, marginBottom: 16 },
  grid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 },
  card: {
    background: '#fff', borderRadius: 10, padding: '32px 16px',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    cursor: 'pointer', boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
    minHeight: 100,
  },
  cardLabel: { fontSize: 18, fontWeight: 600, color: '#333' },
}
