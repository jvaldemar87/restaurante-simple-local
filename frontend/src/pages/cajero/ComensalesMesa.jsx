import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { comensales, pedidos, mesas } from '../../api/client'
import Header from '../../components/Header'

export default function ComensalesMesaCajero() {
  const { mesaId } = useParams()
  const navigate = useNavigate()
  const [lista, setLista] = useState([])
  const [mesa, setMesa] = useState(null)

  const load = async () => {
    const [c, m] = await Promise.all([
      comensales.listByMesa(mesaId),
      mesas.get(mesaId)
    ])
    setLista(c)
    setMesa(m)
  }

  useEffect(() => { load() }, [mesaId])

  const imprimirComanda = () => {
    const pdfWindow = window.open('', '_blank')
    if (!pdfWindow) { alert('Permite ventanas emergentes'); return }
    pdfWindow.document.write('<html><body style="font-family:sans-serif;padding:40px;text-align:center"><p>Cargando comanda...</p></body></html>')
    const token = localStorage.getItem('token')
    fetch(`/api/reportes/comanda/${mesaId}`, {
      headers: { Authorization: `Bearer ${token}` }
    }).then(r => r.blob()).then(blob => {
      pdfWindow.location.href = URL.createObjectURL(blob)
      load()
    }).catch(() => pdfWindow.close())
  }

  const cerrarCuenta = async () => {
    if (!window.confirm('¿Cerrar cuenta de toda la mesa?')) return
    const pdfWindow = window.open('', '_blank')
    if (!pdfWindow) { alert('Permite ventanas emergentes'); return }
    pdfWindow.document.write('<html><body style="font-family:sans-serif;padding:40px;text-align:center"><p>Generando ticket...</p></body></html>')
    try {
      const token = localStorage.getItem('token')
      const res = await fetch(`/api/reportes/ticket/${mesaId}`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      const blob = await res.blob()
      pdfWindow.location.href = URL.createObjectURL(blob)
      await pedidos.cerrarMesa(mesaId)
      await load()
    } catch (e) {
      pdfWindow.close()
      alert('Error: ' + (e.message || 'desconocido'))
    }
  }

  const libre = mesa?.estado === 'LIBRE'

  return (
    <div style={styles.page}>
      <Header />
      <div style={styles.content}>
        <div style={styles.topBar}>
          <button style={styles.topBack} onClick={() => navigate('..')}>&lt;</button>
          <div>
            <h2 style={styles.title}>Comensales</h2>
            <p style={styles.mesaLabel}>Mesa {mesaId}</p>
          </div>
        </div>

        <div style={styles.list}>
          {!libre && lista.map(c => (
            <div key={c.id} style={styles.card}
              onClick={() => navigate(`comensal/${c.id}`)}>
              {c.nombre}
            </div>
          ))}
          {(!libre && lista.length === 0) && (
            <p style={styles.empty}>Sin comensales</p>
          )}
          {libre && (
            <p style={styles.empty}>Mesa libre</p>
          )}
        </div>

        {!libre && (
          <>
            <div style={styles.actions}>
              <button style={styles.printBtn} onClick={imprimirComanda}>IMPRIMIR</button>
            </div>
            <button style={styles.closeBtn} onClick={cerrarCuenta}>CERRAR CUENTA</button>
          </>
        )}
      </div>
    </div>
  )
}

const styles = {
  page: { minHeight: '100vh', background: '#f5f5f5' },
  content: { padding: '16px 16px 100px' },
  topBar: { display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 },
  topBack: {
    width: 40, height: 40, borderRadius: 10, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 24, fontWeight: 700,
    cursor: 'pointer', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center',
  },
  title: { fontSize: 20, fontWeight: 700, marginBottom: 2 },
  mesaLabel: { fontSize: 14, color: '#888', marginBottom: 0 },
  empty: { textAlign: 'center', color: '#999', padding: 40, fontSize: 15 },
  list: { display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 16 },
  card: {
    background: '#fff', borderRadius: 10, padding: '16px 20px',
    cursor: 'pointer', boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
    fontSize: 18, fontWeight: 600,
  },
  actions: { display: 'flex', gap: 10, marginBottom: 10 },
  printBtn: {
    flex: 1, padding: 14, borderRadius: 10, border: '1px solid #d32f2f',
    background: '#fff', color: '#d32f2f', fontSize: 16, fontWeight: 600,
    cursor: 'pointer',
  },
  closeBtn: {
    width: '100%', padding: 16, borderRadius: 10, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 16, fontWeight: 600,
    cursor: 'pointer',
  },
}
