import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { categoriasPago, pagos, openPdf } from '../../api/client'
import Header from '../../components/Header'

const meses = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre']
const anioActual = new Date().getFullYear()
const today = () => new Date().toISOString().split('T')[0]
const emptyForm = { concepto: '', monto: '', fecha: today(), observaciones: '' }

export default function GestionPagos() {
  const navigate = useNavigate()
  const [cats, setCats] = useState([])
  const [selectedCat, setSelectedCat] = useState(null)
  const [items, setItems] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [file, setFile] = useState(null)
  const [preview, setPreview] = useState(null)
  const [existingImage, setExistingImage] = useState(null)
  const [removeImage, setRemoveImage] = useState(false)
  const [listMes, setListMes] = useState(new Date().getMonth() + 1)
  const [listAnio, setListAnio] = useState(anioActual)
  const [reportMes, setReportMes] = useState('')
  const [reportAnio, setReportAnio] = useState('')
  const [reportCat, setReportCat] = useState('')
  const [viewItem, setViewItem] = useState(null)
  const [pageError, setPageError] = useState(null)

  const loadCats = () => categoriasPago.list().then(data => {
    setCats(data)
    if (!selectedCat && data.length > 0) setSelectedCat(data[0].id)
  })

  useEffect(() => { loadCats() }, [])

  useEffect(() => { setPageError(null) }, [form])

  const loadItems = () => {
    if (selectedCat) pagos.list(selectedCat, listMes, listAnio).then(setItems)
    else setItems([])
  }

  useEffect(() => { loadItems() }, [selectedCat, listMes, listAnio])

  const resetForm = () => {
    setForm(emptyForm)
    setEditingId(null)
    setFile(null)
    setPreview(null)
    setExistingImage(null)
    setRemoveImage(false)
    setPageError(null)
  }

  const saveItem = async () => {
    if (!form.concepto || !form.monto || !selectedCat) return
    setPageError(null)
    let evidenciaImagen = existingImage
    if (removeImage) evidenciaImagen = null
    else if (file) evidenciaImagen = await pagos.uploadImage(file)
    const payload = {
      concepto: form.concepto,
      monto: Number(form.monto),
      fecha: form.fecha,
      observaciones: form.observaciones || null,
      categoriaPagoId: selectedCat,
      evidenciaImagen
    }
    try {
      if (editingId) {
        await pagos.update(editingId, payload)
      } else {
        await pagos.create(payload)
      }
      resetForm()
      loadItems()
    } catch (e) {
      const data = e.response?.data
      if (data && typeof data === 'object') {
        const messages = Object.values(data).join('. ')
        setPageError(messages)
      } else {
        setPageError(e.message || 'Error al guardar el pago')
      }
    }
  }

  const editItem = (item) => {
    setForm({
      concepto: item.concepto,
      monto: String(item.monto),
      fecha: item.fecha || today(),
      observaciones: item.observaciones || ''
    })
    setEditingId(item.id)
    setSelectedCat(item.categoriaPagoId)
    setExistingImage(item.evidenciaImagen || null)
    setFile(null)
    setPreview(null)
    setRemoveImage(false)
    window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' })
  }

  const cancelEdit = () => {
    resetForm()
    setPageError(null)
  }

  const deleteItem = async (id) => {
    if (!window.confirm('¿Eliminar pago?')) return
    await pagos.delete(id)
    loadItems()
  }

  const handleFileChange = (e) => {
    const f = e.target.files[0]
    if (!f) return
    setFile(f)
    setPreview(URL.createObjectURL(f))
    setRemoveImage(false)
  }

  const removeFile = () => {
    setFile(null)
    setPreview(null)
    if (editingId && existingImage) setRemoveImage(true)
    else setExistingImage(null)
  }

  const generarReporte = () => {
    let params = ''
    if (reportCat) params += `categoria=${reportCat}&`
    if (reportMes && reportAnio) { params += `mes=${reportMes}&anio=${reportAnio}&` }
    openPdf(`/api/reportes/pagos?${params}`)
  }

  const currentImage = preview || (editingId && !removeImage ? existingImage : null)

  return (
    <div style={styles.page}>
      <Header />
      <div style={styles.content}>
        <div style={styles.topRow}>
          <div style={styles.topTitle}>
            <button style={styles.backBtn} onClick={() => navigate('/admin')}>&lt;</button>
            <h2 style={styles.title}>Pagos</h2>
          </div>
        </div>

        <div style={styles.chips}>
          {cats.map(c => (
            <div key={c.id} style={{
              ...styles.chip, background: selectedCat === c.id ? '#388e3c' : '#fff',
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
              <div style={styles.itemInfo} onClick={() => setViewItem(i)}>
                <span style={styles.itemName}>{i.concepto}</span>
                <span style={styles.itemDetail}>{i.fecha}{i.evidenciaImagen ? ' \u{1F4F7}' : ''}</span>
              </div>
              <span style={styles.amount}>${i.monto}</span>
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
          <h3 style={styles.formTitle}>{editingId ? 'Editar Pago' : 'Agregar Pago'}</h3>
          {pageError && <div style={styles.formError}>{pageError}</div>}
          <input style={styles.input} placeholder="Concepto" value={form.concepto}
            onChange={e => setForm({ ...form, concepto: e.target.value })} />
          <input style={styles.input} placeholder="Monto" type="number" value={form.monto}
            onChange={e => setForm({ ...form, monto: e.target.value })} />
          <input style={styles.input} type="date" value={form.fecha} max={today()}
            onChange={e => setForm({ ...form, fecha: e.target.value })} />
          <input style={styles.input} placeholder="Observaciones (opcional)" value={form.observaciones}
            onChange={e => setForm({ ...form, observaciones: e.target.value })} />
          <input style={styles.input} type="file" accept="image/*" onChange={handleFileChange} />
          {currentImage && (
            <div style={styles.imagePreviewRow}>
              <img src={currentImage} style={styles.imagePreview} alt="Vista previa" />
              <button style={styles.removeImgBtn} onClick={removeFile}>Quitar foto</button>
            </div>
          )}
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

      {viewItem && (
        <div style={styles.modalOverlay} onClick={() => setViewItem(null)}>
          <div style={styles.modalContent} onClick={e => e.stopPropagation()}>
            <button style={styles.modalClose} onClick={() => setViewItem(null)}>✕</button>
            <p style={styles.modalField}><strong>Concepto:</strong> {viewItem.concepto}</p>
            <p style={styles.modalField}><strong>Monto:</strong> ${viewItem.monto}</p>
            <p style={styles.modalField}><strong>Fecha:</strong> {viewItem.fecha}</p>
            <p style={styles.modalField}><strong>Categoría:</strong> {viewItem.categoriaPagoNombre}</p>
            {viewItem.observaciones && (
              <p style={styles.modalField}><strong>Observaciones:</strong> {viewItem.observaciones}</p>
            )}
            {viewItem.evidenciaImagen && (
              <div style={styles.modalImageWrap}>
                <img src={viewItem.evidenciaImagen} style={styles.modalImage} alt="Evidencia" />
                <br />
                <a href={viewItem.evidenciaImagen} target="_blank" rel="noopener noreferrer"
                   style={styles.originalLink}>Ver original</a>
              </div>
            )}
          </div>
        </div>
      )}
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
  chip: {
    padding: '6px 14px', borderRadius: 16, border: '1px solid #ddd',
    fontSize: 12, fontWeight: 500, cursor: 'pointer',
  },
  filterRow: {
    display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12,
    padding: '8px 12px', background: '#fff', borderRadius: 8, boxShadow: '0 1px 2px rgba(0,0,0,0.04)',
  },
  filterLabel: { fontSize: 13, fontWeight: 600, color: '#555' },
  filterSelect: {
    padding: '6px 8px', borderRadius: 6, border: '1px solid #bbb',
    fontSize: 13, cursor: 'pointer', background: '#fff',
  },
  list: { display: 'flex', flexDirection: 'column', gap: 6, marginBottom: 12 },
  itemRow: {
    display: 'flex', alignItems: 'center', gap: 8, padding: '10px 12px',
    background: '#fff', borderRadius: 8, boxShadow: '0 1px 2px rgba(0,0,0,0.04)',
  },
  itemInfo: { flex: 1, cursor: 'pointer' },
  itemName: { display: 'block', fontWeight: 500, fontSize: 14 },
  itemDetail: { display: 'block', fontSize: 12, color: '#888' },
  amount: { fontWeight: 700, color: '#388e3c', fontSize: 15 },
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
    padding: '10px 12px', background: '#e8f5e9', borderRadius: 8,
  },
  reportLabel: { fontSize: 13, fontWeight: 600, color: '#2e7d32' },
  reportSelect: {
    padding: '6px 8px', borderRadius: 6, border: '1px solid #bbb',
    fontSize: 13, cursor: 'pointer', background: '#fff',
  },
  reportBtn: {
    padding: '6px 14px', borderRadius: 8, border: 'none',
    background: '#388e3c', color: '#fff', fontSize: 13, fontWeight: 600,
    cursor: 'pointer',
  },
  form: { background: '#fff', borderRadius: 10, padding: 16, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' },
  formTitle: { fontSize: 15, fontWeight: 600, marginBottom: 10 },
  formError: {
    background: '#ffebee', color: '#c62828', padding: '8px 12px',
    borderRadius: 6, fontSize: 13, marginBottom: 10,
  },
  input: {
    padding: '10px 12px', borderRadius: 8, border: '1px solid #ddd',
    fontSize: 14, outline: 'none', marginBottom: 8, width: '100%', boxSizing: 'border-box',
  },
  imagePreviewRow: { display: 'flex', alignItems: 'flex-start', gap: 8, marginBottom: 8 },
  imagePreview: { maxHeight: 100, borderRadius: 6, border: '1px solid #ddd' },
  removeImgBtn: {
    padding: '4px 10px', borderRadius: 6, border: '1px solid #d32f2f',
    background: '#fff', color: '#d32f2f', fontSize: 12, cursor: 'pointer',
    whiteSpace: 'nowrap',
  },
  formActions: { display: 'flex', gap: 8, marginTop: 4 },
  addBtn: {
    flex: 1, padding: 11, borderRadius: 8, border: 'none',
    background: '#388e3c', color: '#fff', fontSize: 14, fontWeight: 600,
    cursor: 'pointer',
  },
  cancelBtn: {
    padding: '11px 16px', borderRadius: 8, border: '1px solid #999',
    background: '#fff', color: '#555', fontSize: 14, fontWeight: 600,
    cursor: 'pointer',
  },
  modalOverlay: {
    position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    zIndex: 1000,
  },
  modalContent: {
    background: '#fff', borderRadius: 12, padding: 24,
    maxWidth: 500, width: '90%', maxHeight: '90vh', overflow: 'auto',
    position: 'relative',
  },
  modalClose: {
    position: 'absolute', top: 8, right: 8, border: 'none',
    background: 'none', fontSize: 22, cursor: 'pointer', color: '#666',
  },
  modalField: { fontSize: 14, marginBottom: 6 },
  modalImageWrap: { marginTop: 12 },
  modalImage: { maxWidth: '100%', maxHeight: '80vh', borderRadius: 8, display: 'block' },
  originalLink: {
    display: 'inline-block', marginTop: 8, padding: '6px 14px',
    background: '#1976d2', color: '#fff', borderRadius: 6,
    textDecoration: 'none', fontSize: 13, fontWeight: 600,
  },
}