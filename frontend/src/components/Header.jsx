import { useAuth } from '../context/AuthContext'

export default function Header() {
  const { user, logout } = useAuth()

  return (
    <div style={styles.header}>
      <div style={styles.title}>Mi Restaurante</div>
      <div style={styles.right}>
        <span style={styles.name}>{user?.nombre}</span>
        <div style={styles.avatar}>U</div>
        <button onClick={logout} style={styles.logoutBtn}>Salir</button>
      </div>
    </div>
  )
}

const styles = {
  header: {
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    padding: '12px 16px', background: '#fff',
    borderBottom: '1px solid #e0e0e0', position: 'sticky', top: 0, zIndex: 10,
  },
  title: { fontSize: 22, fontWeight: 700, color: '#d32f2f' },
  right: { display: 'flex', alignItems: 'center', gap: 10 },
  name: { fontSize: 13, color: '#555' },
  avatar: {
    width: 36, height: 36, borderRadius: '50%', background: '#d32f2f',
    color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: 16, fontWeight: 600,
  },
  logoutBtn: {
    background: 'none', border: '1px solid #ccc', borderRadius: 6,
    padding: '4px 10px', fontSize: 12, cursor: 'pointer', color: '#666',
  },
}
