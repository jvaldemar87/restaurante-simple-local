import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { comensales, pedidos } from '../../api/client'
import Header from '../../components/Header'

export default function DetalleComensalCajero() {
  const { mesaId, comensalId } = useParams()
  const [comensal, setComensal] = useState(null)
  const [pedidosList, setPedidosList] = useState([])

  useEffect(() => {
    comensales.get(comensalId).then(setComensal)
    pedidos.listByComensal(comensalId).then(setPedidosList)
  }, [comensalId])

  const total = pedidosList.reduce((s, p) => s + p.total, 0)

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

        <div style={styles.productList}>
          {pedidosList.map(p => (
            p.detalles?.map(d => (
              <div key={d.id} style={styles.row}>
                <span style={styles.name}>{d.productoNombre}</span>
                <span style={styles.qty}>x{d.cantidad}</span>
                <span style={styles.price}>${d.subtotal}</span>
              </div>
            ))
          ))}
          {pedidosList.length === 0 && (
            <p style={styles.empty}>Sin productos</p>
          )}
        </div>

        <div style={styles.totalRow}>
          <span>Total</span>
          <span style={styles.totalAmount}>${total.toFixed(2)}</span>
        </div>
      </div>
    </div>
  )
}

const styles = {
  page: { minHeight: '100vh', background: '#f5f5f5' },
  content: { padding: '16px 16px 100px' },
  headerRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 },
  title: { fontSize: 20, fontWeight: 700 },
  sub: { fontSize: 13, color: '#888' },
  detalleLabel: { fontSize: 14, color: '#888', fontWeight: 500 },
  productList: {
    background: '#fff', borderRadius: 10, padding: 12, marginBottom: 12,
    minHeight: 180, boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
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
    padding: '12px 16px', background: '#fff', borderRadius: 10,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)', fontSize: 16,
  },
  totalAmount: { fontSize: 20, fontWeight: 700, color: '#d32f2f' },
}
