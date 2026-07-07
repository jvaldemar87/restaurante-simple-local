import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import { pedidos } from '../../api/client'
import Header from '../../components/Header'

export default function ResumenOrden() {
  const { mesaId, comensalId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const [pedido, setPedido] = useState(null)

  const load = async () => {
    const list = await pedidos.listByComensal(comensalId)
    const activo = list.find(p => p.estado === 'ACTIVO')
    if (activo) {
      setPedido(activo)
    } else {
      const nuevo = await pedidos.create(comensalId)
      setPedido(nuevo)
    }
  }

  useEffect(() => { load() }, [comensalId])

  const guardar = async () => {
    if (pedido) {
      await pedidos.cerrar(pedido.id)
    }
    const idx = location.pathname.indexOf('/comensal/')
    const targetUrl = idx !== -1 ? location.pathname.substring(0, idx) : location.pathname
    navigate(targetUrl)
  }

  const total = pedido?.detalles?.reduce((s, d) => s + d.subtotal, 0) || 0
  const primeraVez = pedido?.detalles?.length === 0

  return (
    <div style={styles.page}>
      <Header />
      <div style={styles.content}>
        <h2 style={styles.title}>RESUMEN DE LA ORDEN</h2>

        <div style={styles.scrollArea}>
          <div style={styles.list}>
            {pedido?.detalles?.map(d => (
              <div key={d.id} style={styles.row}>
                <span style={styles.name}>{d.productoNombre}</span>
                <span style={styles.qty}>x{d.cantidad}</span>
                <span style={styles.price}>${d.subtotal}</span>
              </div>
            ))}
            {(!pedido?.detalles || pedido.detalles.length === 0) && (
              <p style={styles.empty}>Sin productos</p>
            )}
          </div>
        </div>

        <div style={styles.totalRow}>
          <span>GRAN TOTAL</span>
          <span style={styles.totalAmount}>${total.toFixed(2)}</span>
        </div>
      </div>

      <div style={styles.bottomRow}>
        <button style={styles.backBtn}
          onClick={() => navigate('..')}>
          &lt;
        </button>
        <button style={styles.saveBtn} onClick={guardar}>
          Cerrar Orden
        </button>
      </div>
    </div>
  )
}

const styles = {
  page: { height: '100vh', display: 'flex', flexDirection: 'column', background: '#f5f5f5' },
  content: { flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden', padding: '16px 16px 0' },
  title: { fontSize: 20, fontWeight: 700, marginBottom: 12, textAlign: 'center', flexShrink: 0 },
  scrollArea: { flex: 1, minHeight: 0, overflowY: 'auto' },
  list: {
    background: '#fff', borderRadius: 10, padding: 12, minHeight: 200,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)', marginBottom: 12,
  },
  row: {
    display: 'flex', alignItems: 'center', gap: 8,
    padding: '8px 0', borderBottom: '1px solid #f0f0f0', fontSize: 14,
  },
  name: { flex: 1, fontWeight: 500 },
  qty: { color: '#888', width: 30, textAlign: 'right' },
  price: { width: 60, textAlign: 'right', fontWeight: 600 },
  empty: { textAlign: 'center', color: '#bbb', padding: 40, fontSize: 14 },
  totalRow: {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
    padding: '12px 16px', background: '#fff', borderRadius: 10, marginBottom: 0,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)', fontSize: 16, flexShrink: 0,
  },
  totalAmount: { fontSize: 20, fontWeight: 700, color: '#d32f2f' },
  bottomRow: { display: 'flex', gap: 10, padding: 12, background: '#f5f5f5', borderTop: '1px solid #e0e0e0', flexShrink: 0 },
  backBtn: {
    flex: 1, height: 56, borderRadius: 10, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 32, fontWeight: 700,
    cursor: 'pointer',
  },
  saveBtn: {
    flex: 1, height: 56, borderRadius: 10, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 'clamp(13px, 3.8vw, 22px)', fontWeight: 700,
    cursor: 'pointer',
  },
}
