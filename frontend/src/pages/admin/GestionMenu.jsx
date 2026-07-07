import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { categorias, productos } from '../../api/client'
import Header from '../../components/Header'

export default function GestionMenu() {
  const navigate = useNavigate()
  const [cats, setCats] = useState([])
  const [selectedCat, setSelectedCat] = useState(null)
  const [prods, setProds] = useState([])
  const [newCat, setNewCat] = useState('')
  const [newProd, setNewProd] = useState({ nombre: '', precio: '' })

  const loadCats = () => categorias.list().then(setCats)

  useEffect(() => { loadCats() }, [])

  useEffect(() => {
    if (selectedCat) productos.list(selectedCat).then(setProds)
    else setProds([])
  }, [selectedCat])

  const addCategoria = async () => {
    if (!newCat.trim()) return
    await categorias.create({ nombre: newCat.trim() })
    setNewCat('')
    loadCats()
  }

  const addProducto = async () => {
    if (!newProd.nombre || !newProd.precio || !selectedCat) return
    await productos.create({
      nombre: newProd.nombre,
      precio: Number(newProd.precio),
      categoriaId: selectedCat
    })
    setNewProd({ nombre: '', precio: '' })
    const list = await productos.list(selectedCat)
    setProds(list)
  }

  const deleteProd = async (id) => {
    if (!window.confirm('¿Eliminar producto?')) return
    await productos.delete(id)
    setProds(prods.filter(p => p.id !== id))
  }

  return (
    <div style={styles.page}>
      <Header />
      <div style={styles.content}>
        <div style={styles.topRow}>
          <button style={styles.backBtn} onClick={() => navigate('/admin')}>&lt;</button>
          <h2 style={styles.title}>Gestión del Menú</h2>
        </div>

        <div style={styles.section}>
          <h3 style={styles.sectionTitle}>Categorías</h3>
          <div style={styles.chips}>
            {cats.map(c => (
              <div key={c.id} style={{
                ...styles.chip,
                background: selectedCat === c.id ? '#d32f2f' : '#fff',
                color: selectedCat === c.id ? '#fff' : '#333',
                borderColor: '#ddd',
              }} onClick={() => setSelectedCat(c.id)}>
                {c.nombre}
              </div>
            ))}
          </div>
          <div style={styles.addRow}>
            <input style={styles.input} placeholder="Nueva categoría" value={newCat}
              onChange={e => setNewCat(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && addCategoria()} />
            <button style={styles.smallBtn} onClick={addCategoria}>+</button>
          </div>
        </div>

        <div style={styles.section}>
          <h3 style={styles.sectionTitle}>Productos</h3>
          {prods.map(p => (
            <div key={p.id} style={styles.itemRow}>
              <span style={styles.itemName}>{p.nombre}</span>
              <span style={styles.itemPrice}>${p.precio}</span>
              <button style={styles.delBtn} onClick={() => deleteProd(p.id)}>x</button>
            </div>
          ))}
          {selectedCat && (
            <div style={styles.addRow}>
              <input style={{ ...styles.input, flex: 2 }} placeholder="Nombre" value={newProd.nombre}
                onChange={e => setNewProd({ ...newProd, nombre: e.target.value })} />
              <input style={{ ...styles.input, flex: 1 }} placeholder="Precio" type="number" value={newProd.precio}
                onChange={e => setNewProd({ ...newProd, precio: e.target.value })}
                onKeyDown={e => e.key === 'Enter' && addProducto()} />
              <button style={styles.smallBtn} onClick={addProducto}>+</button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

const styles = {
  page: { minHeight: '100vh', background: '#f5f5f5' },
  content: { padding: '16px 16px 100px' },
  topRow: { display: 'flex', alignItems: 'center', gap: 10, marginBottom: 16 },
  backBtn: {
    width: 36, height: 36, borderRadius: 8, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 20, fontWeight: 700,
    cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
  },
  title: { fontSize: 20, fontWeight: 700 },
  section: { marginBottom: 20 },
  sectionTitle: { fontSize: 16, fontWeight: 600, marginBottom: 8, color: '#555' },
  chips: { display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 8 },
  chip: {
    padding: '8px 16px', borderRadius: 20, border: '1px solid',
    fontSize: 13, fontWeight: 500, cursor: 'pointer',
  },
  addRow: { display: 'flex', gap: 8, marginBottom: 8 },
  input: {
    padding: '10px 12px', borderRadius: 8, border: '1px solid #ddd',
    fontSize: 14, outline: 'none', flex: 1,
  },
  smallBtn: {
    width: 40, height: 40, borderRadius: 8, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 22, fontWeight: 700,
    cursor: 'pointer', flexShrink: 0,
  },
  itemRow: {
    display: 'flex', alignItems: 'center', gap: 8, padding: '10px 12px',
    background: '#fff', borderRadius: 8, marginBottom: 6,
    boxShadow: '0 1px 2px rgba(0,0,0,0.04)',
  },
  itemName: { flex: 1, fontWeight: 500, fontSize: 14 },
  itemPrice: { color: '#d32f2f', fontWeight: 600, fontSize: 14 },
  delBtn: {
    width: 26, height: 26, borderRadius: '50%', border: '1px solid #d32f2f',
    color: '#d32f2f', background: '#fff', cursor: 'pointer', fontSize: 14,
    display: 'flex', alignItems: 'center', justifyContent: 'center',
  },
}
