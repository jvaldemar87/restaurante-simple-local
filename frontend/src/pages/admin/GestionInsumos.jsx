import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { categoriasInsumo, insumos, openPdf } from '../../api/client'
import Header from '../../components/Header'

const today = () => new Date().toISOString().split('T')[0]
const emptyForm = { nombre: '', cantidad: '', unidad: 'pza', precioUnitario: '', fechaIngreso: today() }
const meses = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre']
const anioActual = new Date().getFullYear()

export default function GestionInsumos() {
  const navigate = useNavigate()
  const [cats, setCats] = useState([])
  const [selectedCat, setSelectedCat] = useState(null)
  const [items, setItems] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [listMes, setListMes] = useState(new Date().getMonth() + 1)
  const [listAnio, setListAnio] = useState(anioActual)
  const [reportMes, setReportMes] = useState('')
  const [reportAnio, setReportAnio] = useState('')
  const [reportCat, setReportCat] = useState('')

  const loadCats = () => categoriasInsumo.list().then(data => {
    setCats(data)
    if (!selectedCat && data.length > 0) setSelectedCat(data[0].id)
  })

  useEffect(() => { loadCats() }, [])

  const loadItems = () => {
    if (selectedCat) insumos.list(selectedCat, listMes, listAnio).then(setItems)
    else setItems([])
  }

  useEffect(() => { loadItems() }, [selectedCat, listMes, listAnio])

  const saveItem = async () => {
    if (!form.nombre || !form.cantidad || !form.precioUnitario || !selectedCat) return
    const payload = {
      nombre: form.nombre, cantidad: Number(form.cantidad),
      unidad: form.unidad, precioUnitario: Number(form.precioUnitario),
      categoriaInsumoId: selectedCat, fechaIngreso: form.fechaIngreso
    }
    if (editingId) {
      await insumos.update(editingId, payload)
    } else {
      await insumos.create(payload)
    }
    setForm(emptyForm)
    setEditingId(null)
    loadItems()
  }

  const editItem = (item) => {
    setForm({
      nombre: item.nombre,
      cantidad: String(item.cantidad),
      unidad: item.unidad,
      precioUnitario: String(item.precioUnitario),
      fechaIngreso: item.fechaIngreso || today()
    })
    setEditingId(item.id)
    setSelectedCat(item.categoriaInsumoId)
    window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' })
  }

  const cancelEdit = () => {
    setForm(emptyForm)
    setEditingId(null)
  }

  const deleteItem = async (id) => {
    if (!window.confirm('¿Eliminar insumo?')) return
    await insumos.delete(id)
    loadItems()
  }

  const generarReporte = () => {
    let params = ''
    if (reportCat) params += `categoria=${reportCat}&`
    if (reportMes && reportAnio) { params += `mes=${reportMes}&anio=${reportAnio}&` }
    openPdf(`/api/reportes/insumos?${params}`)
  }

  return (
    <div style={styles.page}>
      <Header />
      <div style={styles.content}>
        <div style={styles.topRow}>
          <div style={styles.topTitle}>
            <button style={styles.backBtn} onClick={() => navigate('/admin')}>&lt;</button>
            <h2 style={styles.title}>Insumos</h2>
          </div>
        </div>

        <div style={styles.chips}>
          {cats.map(c => (
            <div key={c.id} style={{
              ...styles.chip, background: selectedCat === c.id ? '#d32f2f' : '#fff',
              color: selectedCat === c.id ? '#fff' : '#333',
            }} onClick={() => setSelectedCat(c.id)}>
              {c.nombre}
            </div>
          ))}
        </div>

        <div style={styles.filterRow}>
          <span style={styles.filterLabel}>Filtrar:</span>
          <select style={styles.filterSelect} value={listMes} onChange={e => setListMes(Number(e.target.value))}>
            {meses.map((m, i) => <option key={i} value={i + 1}>{m}</option>)}
          </select>
          <select style={styles.filterSelect} value={listAnio} onChange={e => setListAnio(Number(e.target.value))}>
            {Array.from({ length: 5 }, (_, i) => anioActual - 2 + i).map(a =>
              <option key={a} value={a}>{a}</option>
            )}
          </select>
        </div>

        <div style={styles.list}>
          {items.map(i => (
            <div key={i.id} style={styles.itemRow}>
              <div style={styles.itemInfo}>
                <span style={styles.itemName}>{i.nombre}</span>
                <span style={styles.itemDetail}>
                  {i.cantidad} {i.unidad} — ${i.precioUnitario}/u
                  {i.fechaIngreso && <span style={styles.itemDate}> — {i.fechaIngreso}</span>}
                </span>
              </div>
              <button style={styles.editBtn} onClick={() => editItem(i)}>✎</button>
              <button style={styles.delBtn} onClick={() => deleteItem(i.id)}>-</button>
            </div>
          ))}
        </div>

        <div style={styles.reportRow}>
          <span style={styles.reportLabel}>Reporte:</span>
          <select style={styles.reportSelect} value={reportCat} onChange={e => setReportCat(e.target.value)}>
            <option value="">Todas las categorías</option>
            {cats.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
          </select>
          <select style={styles.reportSelect} value={reportMes} onChange={e => setReportMes(e.target.value)}>
            <option value="">Mes</option>
            {meses.map((m, i) => <option key={i} value={i + 1}>{m}</option>)}
          </select>
          <select style={styles.reportSelect} value={reportAnio} onChange={e => setReportAnio(e.target.value)}>
            <option value="">Año</option>
            {Array.from({ length: 5 }, (_, i) => anioActual - 2 + i).map(a =>
              <option key={a} value={a}>{a}</option>
            )}
          </select>
          <button style={styles.reportBtn} onClick={generarReporte}>PDF</button>
        </div>

        <div style={styles.form}>
          <h3 style={styles.formTitle}>{editingId ? 'Editar Insumo' : 'Agregar Insumo'}</h3>
          <input style={styles.input} placeholder="Nombre" value={form.nombre}
            onChange={e => setForm({ ...form, nombre: e.target.value })} />
          <div style={styles.row}>
            <input style={{ ...styles.input, flex: 1 }} placeholder="Cantidad" type="number" value={form.cantidad}
              onChange={e => setForm({ ...form, cantidad: e.target.value })} />
            <input style={{ ...styles.input, flex: 1 }} placeholder="Unidad" value={form.unidad}
              onChange={e => setForm({ ...form, unidad: e.target.value })} />
            <input style={{ ...styles.input, flex: 1 }} placeholder="Precio Unitario" type="number" value={form.precioUnitario}
              onChange={e => setForm({ ...form, precioUnitario: e.target.value })} />
          </div>
          <input style={styles.input} type="date" value={form.fechaIngreso}
            onChange={e => setForm({ ...form, fechaIngreso: e.target.value })} />
          <div style={styles.formActions}>
            {editingId && (
              <button style={styles.cancelBtn} onClick={cancelEdit}>Cancelar</button>
            )}
            <button style={styles.addBtn} onClick={saveItem}>
              {editingId ? 'Guardar Cambios' : '+ Agregar'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

const styles = {
  page: { minHeight: '100vh', background: '#f5f5f5' },
  content: { padding: '16px 16px 100px' },
  topRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  topTitle: { display: 'flex', alignItems: 'center', gap: 10 },
  backBtn: {
    width: 36, height: 36, borderRadius: 8, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 20, fontWeight: 700,
    cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
  },
  title: { fontSize: 20, fontWeight: 700 },
  chips: { display: 'flex', flexWrap: 'wrap', gap: 6, marginBottom: 8 },
  filterRow: {
    display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12,
    padding: '8px 12px', background: '#fff', borderRadius: 8, boxShadow: '0 1px 2px rgba(0,0,0,0.04)',
  },
  filterLabel: { fontSize: 13, fontWeight: 600, color: '#555' },
  filterSelect: {
    padding: '6px 8px', borderRadius: 6, border: '1px solid #bbb',
    fontSize: 13, cursor: 'pointer', background: '#fff',
  },
  chip: {
    padding: '6px 14px', borderRadius: 16, border: '1px solid #ddd',
    fontSize: 12, fontWeight: 500, cursor: 'pointer',
  },
  list: { display: 'flex', flexDirection: 'column', gap: 6, marginBottom: 12 },
  itemRow: {
    display: 'flex', alignItems: 'center', gap: 8, padding: '10px 12px',
    background: '#fff', borderRadius: 8, boxShadow: '0 1px 2px rgba(0,0,0,0.04)',
  },
  itemInfo: { flex: 1 },
  itemName: { display: 'block', fontWeight: 500, fontSize: 14 },
  itemDetail: { display: 'block', fontSize: 12, color: '#888' },
  itemDate: { color: '#1976d2', fontWeight: 500 },
  editBtn: {
    width: 28, height: 28, borderRadius: '50%', border: '1px solid #1976d2',
    color: '#1976d2', background: '#fff', cursor: 'pointer', fontSize: 14,
    display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
  },
  delBtn: {
    width: 28, height: 28, borderRadius: '50%', border: '1px solid #d32f2f',
    color: '#d32f2f', background: '#fff', cursor: 'pointer', fontSize: 18,
    display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
  },
  reportRow: {
    display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12, flexWrap: 'wrap',
    padding: '10px 12px', background: '#e3f2fd', borderRadius: 8,
  },
  reportLabel: { fontSize: 13, fontWeight: 600, color: '#1565c0' },
  reportSelect: {
    padding: '6px 8px', borderRadius: 6, border: '1px solid #bbb',
    fontSize: 13, cursor: 'pointer', background: '#fff',
  },
  reportBtn: {
    padding: '6px 14px', borderRadius: 8, border: 'none',
    background: '#1976d2', color: '#fff', fontSize: 13, fontWeight: 600,
    cursor: 'pointer',
  },
  form: { background: '#fff', borderRadius: 10, padding: 16, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' },
  formTitle: { fontSize: 15, fontWeight: 600, marginBottom: 10 },
  input: {
    padding: '10px 12px', borderRadius: 8, border: '1px solid #ddd',
    fontSize: 14, outline: 'none', marginBottom: 8, width: '100%', boxSizing: 'border-box',
  },
  row: { display: 'flex', gap: 8 },
  formActions: { display: 'flex', gap: 8, marginTop: 4 },
  addBtn: {
    flex: 1, padding: 11, borderRadius: 8, border: 'none',
    background: '#1976d2', color: '#fff', fontSize: 14, fontWeight: 600,
    cursor: 'pointer',
  },
  cancelBtn: {
    padding: '11px 16px', borderRadius: 8, border: '1px solid #999',
    background: '#fff', color: '#555', fontSize: 14, fontWeight: 600,
    cursor: 'pointer',
  },
}
