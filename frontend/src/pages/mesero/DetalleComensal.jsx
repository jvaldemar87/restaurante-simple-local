import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { comensales, pedidos } from '../../api/client'
import Header from '../../components/Header'

export default function DetalleComensal() {
  const { mesaId, comensalId } = useParams()
  const navigate = useNavigate()
  const [comensal, setComensal] = useState(null)
  const [pedidoActual, setPedidoActual] = useState(null)
  const [pedidosPrevios, setPedidosPrevios] = useState([])

  const load = async () => {
    const c = await comensales.get(comensalId)
    setComensal(c)
    const list = await pedidos.listByComensal(comensalId)
    const activo = list.find(p => p.estado === 'ACTIVO')
    const cerrados = list.filter(p => p.estado === 'COMIENDO')
    setPedidosPrevios(cerrados)
    if (activo) {
      setPedidoActual(activo)
    } else {
      const nuevo = await pedidos.create(comensalId)
      setPedidoActual(nuevo)
    }
  }

  useEffect(() => { load() }, [comensalId])

  const eliminarProducto = async (detalleId) => {
    if (!window.confirm('¿Eliminar este producto del pedido?')) return
    await pedidos.removeProducto(detalleId)
    load()
  }

  const total = pedidoActual?.detalles?.reduce((s, d) => s + d.subtotal, 0) || 0
  const totalPrevio = pedidosPrevios.reduce((s, p) => s + p.total, 0)

  return (
    <div style={styles.page}>
      <Header />
      <div style={styles.content}>
        <div style={styles.headerRow}>
          <div>
            <h2 style={styles.title}>{comensal?.nombre || 'Cargando...'}</h2>
            <p style={styles.sub}>Mesa {mesaId}</p>
          </div>
          <span style={styles.detalleLabel}>Detalle</span>
        </div>

        <div style={styles.scrollArea}>
          {pedidosPrevios.length > 0 && (
            <div style={styles.prevSection}>
              <p style={styles.prevTitle}>Pedidos anteriores (cerrados):</p>
              {pedidosPrevios.map(p => (
                <div key={p.id}>
                  {p.detalles?.map(d => (
                    <div key={d.id} style={styles.productRow}>
                      <span style={styles.prodName}>{d.productoNombre}</span>
                      <span style={styles.prodQty}>x{d.cantidad}</span>
                      <span style={styles.prodPrice}>${d.subtotal}</span>
                    </div>
                  ))}
                </div>
              ))}
              <div style={styles.prevTotal}>Total anterior: ${totalPrevio.toFixed(2)}</div>
            </div>
          )}

          <div style={styles.productList}>
            {pedidoActual?.detalles?.map(d => (
              <div key={d.id} style={styles.productRow}>
                <button style={styles.removeBtn} onClick={() => eliminarProducto(d.id)}>-</button>
                <span style={styles.prodName}>{d.productoNombre}</span>
                <span style={styles.prodQty}>x{d.cantidad}</span>
                <span style={styles.prodPrice}>${d.subtotal}</span>
              </div>
            ))}
            {(!pedidoActual?.detalles || pedidoActual.detalles.length === 0) && (
              <p style={styles.empty}>Sin productos aún</p>
            )}
          </div>
        </div>

        <div style={styles.subtotalRow}>
          <span>Subtotal (activo)</span>
          <span style={styles.totalAmount}>${total.toFixed(2)}</span>
        </div>
      </div>

      <div style={styles.bottomRow}>
        <button style={styles.backBtn}
          onClick={() => navigate('..')}>
          &lt;
        </button>
        <button style={styles.addBtn}
          onClick={() => navigate('menu')}>
          +
        </button>
        <button style={styles.nextBtn}
          onClick={() => navigate('resumen')}>
          &gt;
        </button>
      </div>
    </div>
  )
}

const styles = {
  page: { height: '100vh', display: 'flex', flexDirection: 'column', background: '#f5f5f5' },
  content: { flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden', padding: '16px 16px 0' },
  headerRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12, flexShrink: 0 },
  title: { fontSize: 20, fontWeight: 700 },
  sub: { fontSize: 13, color: '#888' },
  detalleLabel: { fontSize: 14, color: '#888', fontWeight: 500 },
  scrollArea: { flex: 1, minHeight: 0, overflowY: 'auto' },
  prevSection: {
    background: '#fff3e0', borderRadius: 10, padding: 12, marginBottom: 12,
    fontSize: 13, boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
  },
  prevTitle: { fontWeight: 600, marginBottom: 4, color: '#e65100', fontSize: 12 },
  prevTotal: { textAlign: 'right', fontWeight: 600, color: '#e65100', marginTop: 4, fontSize: 13 },
  productList: {
    background: '#fff', borderRadius: 10, padding: 12, marginBottom: 12,
    minHeight: 120, boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
  },
  productRow: {
    display: 'flex', alignItems: 'center', gap: 8,
    padding: '8px 0', borderBottom: '1px solid #f0f0f0', fontSize: 14,
  },
  removeBtn: {
    width: 24, height: 24, borderRadius: '50%', border: '1px solid #d32f2f',
    color: '#d32f2f', background: '#fff', cursor: 'pointer',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: 16, fontWeight: 700, flexShrink: 0,
  },
  prodName: { flex: 1 },
  prodQty: { color: '#888', width: 30, textAlign: 'right' },
  prodPrice: { width: 60, textAlign: 'right', fontWeight: 600, color: '#333' },
  empty: { textAlign: 'center', color: '#bbb', padding: 30, fontSize: 14 },
  subtotalRow: {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
    padding: '12px 16px', background: '#fff', borderRadius: 10, marginBottom: 0,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)', fontSize: 16, flexShrink: 0,
  },
  totalAmount: { fontSize: 20, fontWeight: 700, color: '#d32f2f' },
  bottomRow: { display: 'flex', gap: 10, padding: 12, background: '#f5f5f5', borderTop: '1px solid #e0e0e0', flexShrink: 0 },
  backBtn: {
    width: 56, height: 56, borderRadius: 10, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 28, fontWeight: 700,
    cursor: 'pointer', flexShrink: 0,
  },
  addBtn: {
    flex: 1, height: 56, borderRadius: 10, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 32, fontWeight: 700,
    cursor: 'pointer',
  },
  nextBtn: {
    flex: 1, height: 56, borderRadius: 10, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 32, fontWeight: 700,
    cursor: 'pointer',
  },
}
