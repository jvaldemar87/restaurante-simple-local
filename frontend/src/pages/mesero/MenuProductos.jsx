import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import { categorias, productos, pedidos } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import Header from '../../components/Header'

export default function MenuProductos() {
  const { mesaId, comensalId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { isAdmin } = useAuth()
  const [cats, setCats] = useState([])
  const [currentIndex, setCurrentIndex] = useState(0)
  const [prods, setProds] = useState([])
  const [searchTerm, setSearchTerm] = useState('')
  const [showInactive, setShowInactive] = useState(false)
  const [showAddProd, setShowAddProd] = useState(false)
  const [showAddCat, setShowAddCat] = useState(false)
  const [editingProduct, setEditingProduct] = useState(null)
  const [newProd, setNewProd] = useState({ nombre: '', precio: '', categoriaId: '' })
  const [newCat, setNewCat] = useState('')
  const [contador, setContador] = useState({})

  const loadCats = () => categorias.list().then(setCats)

  useEffect(() => { loadCats() }, [])

  useEffect(() => {
    if (cats.length > 0) {
      productos.list(cats[currentIndex]?.id, showInactive).then(setProds)
    }
  }, [cats, currentIndex, showInactive])

  useEffect(() => {
    setSearchTerm('')
  }, [currentIndex])

  const refreshProds = async () => {
    if (cats.length > 0) {
      const updated = await productos.list(cats[currentIndex]?.id, showInactive)
      setProds(updated)
    }
  }

  const filteredProds = prods.filter(p =>
    p.nombre.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const agregar = async (prod) => {
    setContador(c => ({ ...c, [prod.id]: (c[prod.id] || 0) + 1 }))
    const list = await pedidos.listByComensal(comensalId)
    const activo = list.find(p => p.estado === 'ACTIVO')
    if (activo) {
      await pedidos.addProducto(activo.id, { productoId: prod.id, cantidad: 1 })
    } else {
      const nuevo = await pedidos.create(comensalId)
      await pedidos.addProducto(nuevo.id, { productoId: prod.id, cantidad: 1 })
    }
  }

  const addCategory = async () => {
    if (!newCat.trim()) return
    const c = await categorias.create({ nombre: newCat.trim() })
    setNewCat('')
    setShowAddCat(false)
    await loadCats()
    const idx = cats.length
    setCurrentIndex(idx)
  }

  const addProduct = async () => {
    if (!newProd.nombre || !newProd.precio || !newProd.categoriaId) return
    await productos.create({
      nombre: newProd.nombre,
      precio: Number(newProd.precio),
      categoriaId: Number(newProd.categoriaId)
    })
    setNewProd({ nombre: '', precio: '', categoriaId: '' })
    setShowAddProd(false)
    const catId = Number(newProd.categoriaId)
    const idx = cats.findIndex(c => c.id === catId)
    if (idx >= 0) setCurrentIndex(idx)
    await refreshProds()
  }

  const startEdit = (prod) => {
    setShowAddCat(false)
    setShowAddProd(false)
    setEditingProduct({
      id: prod.id,
      nombre: prod.nombre,
      precio: prod.precio,
      categoriaId: prod.categoriaId,
    })
  }

  const cancelEdit = () => {
    setEditingProduct(null)
  }

  const saveEdit = async () => {
    if (!editingProduct.nombre || !editingProduct.precio || !editingProduct.categoriaId) return
    await productos.update(editingProduct.id, {
      nombre: editingProduct.nombre,
      precio: Number(editingProduct.precio),
      categoriaId: Number(editingProduct.categoriaId),
    })
    setEditingProduct(null)
    await refreshProds()
  }

  const handleDelete = async (prod) => {
    if (!window.confirm(`\u00bfDesactivar "${prod.nombre}"?`)) return
    await productos.delete(prod.id)
    await refreshProds()
  }

  const handleRestore = async (prod) => {
    await productos.restore(prod.id)
    await refreshProds()
  }

  const currentCat = cats[currentIndex]
  const hasMore = currentIndex < cats.length - 1
  const hasPrev = currentIndex > 0

  return (
    <div style={styles.page}>
      <Header />
      <div style={styles.content}>
        <div style={styles.headerRow}>
          <h2 style={styles.title}>MENU</h2>
          <span style={styles.catLabel}>{currentCat?.nombre || ''}</span>
        </div>

        <div style={styles.searchContainer}>
          <span style={styles.searchIcon}>&#128269;</span>
          <input
            style={styles.searchInput}
            placeholder="Buscar producto..."
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
          />
          {searchTerm && (
            <button style={styles.searchClear} onClick={() => setSearchTerm('')}>&#10005;</button>
          )}
        </div>

        {isAdmin && (
          <label style={styles.inactiveToggle}>
            <input
              type="checkbox"
              checked={showInactive}
              onChange={e => setShowInactive(e.target.checked)}
              style={styles.inactiveCheck}
            />
            Mostrar inactivos
          </label>
        )}

        {isAdmin && !editingProduct && !showAddCat && !showAddProd && (
          <div style={styles.adminBtns}>
            <button style={styles.catBtn} onClick={() => setShowAddCat(true)}>+ Categoría</button>
            <button style={styles.prodBtn} onClick={() => setShowAddProd(true)}>+ Producto</button>
          </div>
        )}

        {isAdmin && showAddCat && (
          <div style={styles.inlineForm}>
            <input style={styles.input} placeholder="Nombre de la categoría" value={newCat}
              onChange={e => setNewCat(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && addCategory()} />
            <div style={styles.formActions}>
              <button style={styles.cancelBtn} onClick={() => { setShowAddCat(false); setNewCat('') }}>Cancelar</button>
              <button style={styles.saveBtn} onClick={addCategory}>Guardar</button>
            </div>
          </div>
        )}

        {isAdmin && showAddProd && !editingProduct && (
          <div style={styles.inlineForm}>
            <p style={styles.formTitle}>Nuevo producto</p>
            <input style={styles.input} placeholder="Nombre del producto" value={newProd.nombre}
              onChange={e => setNewProd({ ...newProd, nombre: e.target.value })} />
            <input style={styles.input} placeholder="Precio" type="number" value={newProd.precio}
              onChange={e => setNewProd({ ...newProd, precio: e.target.value })} />
            <select style={styles.input} value={newProd.categoriaId}
              onChange={e => setNewProd({ ...newProd, categoriaId: e.target.value })}>
              <option value="">Seleccionar categoría</option>
              {cats.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
            </select>
            <div style={styles.formActions}>
              <button style={styles.cancelBtn} onClick={() => { setShowAddProd(false); setNewProd({ nombre: '', precio: '', categoriaId: '' }) }}>Cancelar</button>
              <button style={styles.saveBtn} onClick={addProduct}>Guardar</button>
            </div>
          </div>
        )}

        {isAdmin && editingProduct && (
          <div style={styles.inlineForm}>
            <p style={styles.formTitle}>Editar producto</p>
            <input style={styles.input} placeholder="Nombre del producto"
              value={editingProduct.nombre}
              onChange={e => setEditingProduct({ ...editingProduct, nombre: e.target.value })} />
            <input style={styles.input} placeholder="Precio" type="number"
              value={editingProduct.precio}
              onChange={e => setEditingProduct({ ...editingProduct, precio: e.target.value })} />
            <select style={styles.input}
              value={editingProduct.categoriaId}
              onChange={e => setEditingProduct({ ...editingProduct, categoriaId: e.target.value })}>
              <option value="">Seleccionar categoría</option>
              {cats.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
            </select>
            <div style={styles.formActions}>
              <button style={styles.cancelBtn} onClick={cancelEdit}>Cancelar</button>
              <button style={styles.saveBtn} onClick={saveEdit}>Guardar</button>
            </div>
          </div>
        )}

        <div style={styles.grid}>
          {filteredProds.map(p => {
            const cnt = contador[p.id] || 0
            const inactive = p.disponible === false
            return (
              <div
                key={p.id}
                style={{
                  ...styles.prodCard,
                  position: 'relative',
                  opacity: inactive ? 0.5 : 1,
                  cursor: inactive ? 'default' : 'pointer',
                }}
                onClick={() => !inactive && agregar(p)}
              >
                {isAdmin && !inactive && (
                  <>
                    <button
                      style={styles.editBtn}
                      onClick={e => { e.stopPropagation(); startEdit(p); }}
                      title="Editar"
                    >&#9998;</button>
                    <button
                      style={styles.deleteBtn}
                      onClick={e => { e.stopPropagation(); handleDelete(p); }}
                      title="Desactivar"
                    >&#10005;</button>
                  </>
                )}
                {isAdmin && inactive && (
                  <button
                    style={styles.restoreBtn}
                    onClick={e => { e.stopPropagation(); handleRestore(p); }}
                  >&#8634; Restaurar</button>
                )}
                <span>{p.nombre}</span>
                <span style={styles.prodPrice}>${p.precio}</span>
                {cnt > 0 && <span style={styles.badge}>{cnt}</span>}
              </div>
            )
          })}
          {filteredProds.length === 0 && (
            <p style={styles.empty}>
              {searchTerm ? 'No se encontraron productos' : 'Sin productos en esta categoría'}
            </p>
          )}
        </div>
      </div>

      <div style={styles.bottomRow}>
        <button style={{
          ...styles.navBtn, opacity: hasPrev ? 1 : 0.4,
          cursor: hasPrev ? 'pointer' : 'default',
        }} disabled={!hasPrev}
          onClick={() => hasPrev && setCurrentIndex(i => i - 1)}>
          &lt;
        </button>
        <button style={styles.checkBtn} onClick={() => {
          const total = Object.values(contador).reduce((a, b) => a + b, 0)
          if (total > 0) alert(`\u2705 Productos agregados correctamente`)
          const idx = location.pathname.indexOf('/comensal/')
          const targetUrl = idx !== -1 ? location.pathname.substring(0, idx) : location.pathname
          navigate(targetUrl)
        }}>
          &#10003;
        </button>
        <button style={{
          ...styles.navBtn, opacity: hasMore ? 1 : 0.4,
          cursor: hasMore ? 'pointer' : 'default',
        }} disabled={!hasMore}
          onClick={() => hasMore && setCurrentIndex(i => i + 1)}>
          &gt;
        </button>
      </div>
    </div>
  )
}

const styles = {
  page: { height: '100vh', display: 'flex', flexDirection: 'column', background: '#f5f5f5' },
  content: { flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden', padding: '16px 16px 0' },
  headerRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  title: { fontSize: 20, fontWeight: 700 },
  catLabel: { fontSize: 14, color: '#888', fontWeight: 500 },
  searchContainer: { display: 'flex', alignItems: 'center', position: 'relative', marginBottom: 12 },
  searchIcon: { position: 'absolute', left: 12, fontSize: 14, color: '#aaa', pointerEvents: 'none', zIndex: 1 },
  searchInput: {
    width: '100%', padding: '10px 36px 10px 36px', borderRadius: 8,
    border: '1px solid #ddd', fontSize: 14, outline: 'none',
    background: '#fff',
  },
  searchClear: {
    position: 'absolute', right: 4, background: 'none', border: 'none',
    fontSize: 18, cursor: 'pointer', color: '#aaa', padding: '4px 8px',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
  },
  inactiveToggle: {
    display: 'flex', alignItems: 'center', gap: 8,
    fontSize: 13, color: '#888', marginBottom: 12,
    cursor: 'pointer',
  },
  inactiveCheck: { width: 16, height: 16, cursor: 'pointer' },
  adminBtns: { display: 'flex', gap: 8, marginBottom: 12 },
  catBtn: {
    flex: 1, padding: '10px 0', borderRadius: 8, border: 'none',
    background: '#f57c00', color: '#fff', fontSize: 14, fontWeight: 600,
    cursor: 'pointer',
  },
  prodBtn: {
    flex: 1, padding: '10px 0', borderRadius: 8, border: 'none',
    background: '#1976d2', color: '#fff', fontSize: 14, fontWeight: 600,
    cursor: 'pointer',
  },
  grid: {
    flex: 1, minHeight: 0, overflowY: 'auto',
    display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10,
    background: '#fff', borderRadius: 10, padding: 12,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)', marginBottom: 0,
    alignContent: 'start',
  },
  prodCard: {
    background: '#f9f9f9', borderRadius: 8, padding: '14px 8px',
    textAlign: 'center', cursor: 'pointer', fontSize: 14, fontWeight: 500,
    border: '1px solid #eee', display: 'flex', flexDirection: 'column',
    alignItems: 'center', justifyContent: 'center', minHeight: 56,
  },
  editBtn: {
    position: 'absolute', top: 4, left: 4,
    width: 22, height: 22, borderRadius: '50%',
    border: '1px solid #1976d2', background: '#fff', color: '#1976d2',
    fontSize: 12, cursor: 'pointer', display: 'flex',
    alignItems: 'center', justifyContent: 'center',
    padding: 0, lineHeight: 1,
  },
  deleteBtn: {
    position: 'absolute', top: 4, right: 4,
    width: 22, height: 22, borderRadius: '50%',
    border: '1px solid #d32f2f', background: '#fff', color: '#d32f2f',
    fontSize: 12, cursor: 'pointer', display: 'flex',
    alignItems: 'center', justifyContent: 'center',
    padding: 0, lineHeight: 1,
  },
  restoreBtn: {
    position: 'absolute', top: '50%', left: '50%',
    transform: 'translate(-50%, -50%)',
    background: '#4caf50', color: '#fff', border: 'none',
    borderRadius: 6, padding: '4px 10px', fontSize: 12,
    cursor: 'pointer', fontWeight: 600,
  },
  prodPrice: { fontSize: 12, color: '#d32f2f', fontWeight: 600, marginTop: 2 },
  badge: {
    position: 'absolute', top: -6, right: -6, width: 22, height: 22,
    borderRadius: '50%', background: '#d32f2f', color: '#fff',
    fontSize: 12, fontWeight: 700, display: 'flex', alignItems: 'center',
    justifyContent: 'center',
  },
  empty: { gridColumn: '1 / -1', textAlign: 'center', color: '#bbb', padding: 30, fontSize: 14 },
  inlineForm: {
    background: '#fff', borderRadius: 10, padding: 16, marginBottom: 12,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
  },
  formTitle: { fontSize: 14, fontWeight: 600, marginBottom: 8, color: '#333' },
  input: {
    width: '100%', padding: '10px 12px', borderRadius: 8, border: '1px solid #ddd',
    fontSize: 14, outline: 'none', marginBottom: 8, boxSizing: 'border-box',
  },
  formActions: { display: 'flex', gap: 10 },
  cancelBtn: {
    flex: 1, padding: 10, borderRadius: 8, border: '1px solid #ddd',
    background: '#fff', fontSize: 14, cursor: 'pointer',
  },
  saveBtn: {
    flex: 1, padding: 10, borderRadius: 8, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 14, fontWeight: 600,
    cursor: 'pointer',
  },
  bottomRow: { display: 'flex', gap: 10, padding: 12, background: '#f5f5f5', borderTop: '1px solid #e0e0e0' },
  navBtn: {
    flex: 1, height: 56, borderRadius: 10, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 32, fontWeight: 700,
  },
  checkBtn: {
    width: 64, height: 56, borderRadius: 10, border: 'none',
    background: '#4caf50', color: '#fff', fontSize: 28, fontWeight: 700,
    cursor: 'pointer', flexShrink: 0,
  },
}
