import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { mesas, configuracion } from '../../api/client'
import Header from '../../components/Header'

export default function GestionMesas() {
  const [lista, setLista] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [num, setNum] = useState('')
  const [minutos, setMinutos] = useState(30)
  const [editing, setEditing] = useState(false)
  const [intervaloAlerta, setIntervaloAlerta] = useState(5)
  const [editingAlerta, setEditingAlerta] = useState(false)
  const navigate = useNavigate()

  const load = () => mesas.list().then(setLista)

  useEffect(() => {
    load()
    configuracion.getTiempoTolerancia().then(d => setMinutos(d.minutos)).catch(() => {})
    configuracion.getAlertaIntervalo().then(d => setIntervaloAlerta(d.minutos)).catch(() => {})
  }, [])

  const addMesa = async () => {
    if (!num) return
    await mesas.create({ numero: Number(num) })
    setNum('')
    setShowForm(false)
    load()
  }

  const deleteMesa = async (id, e) => {
    e.stopPropagation()
    if (!window.confirm('¿Eliminar mesa?')) return
    await mesas.delete(id)
    load()
  }

  const toggleEdit = async () => {
    if (editing) {
      if (!minutos || minutos < 1) return
      await configuracion.updateTiempoTolerancia(minutos)
    }
    setEditing(!editing)
  }

  const toggleEditAlerta = async () => {
    if (editingAlerta) {
      if (intervaloAlerta == null || intervaloAlerta < 0) return
      await configuracion.updateAlertaIntervalo(intervaloAlerta)
    }
    setEditingAlerta(!editingAlerta)
  }

  return (
    <div style={styles.page}>
      <Header />
      <div style={styles.content}>
        <div style={styles.topRow}>
          <button style={styles.backBtn} onClick={() => navigate('/admin')}>&lt;</button>
          <h2 style={styles.title}>Mesas</h2>
        </div>
        <div style={styles.list}>
          {lista.map(m => (
            <div key={m.id} style={styles.card}
              onClick={() => navigate(`/admin/mesas/mesa/${m.id}`)}>
              <span style={styles.mesaName}>Mesa {m.numero}</span>
              <div style={styles.right}>
                <div style={{
                  ...styles.status,
                  background: m.estado === 'OCUPADA' ? '#d32f2f' : '#4caf50'
                }} />
                <button style={styles.delBtn} onClick={e => deleteMesa(m.id, e)}>x</button>
              </div>
            </div>
          ))}
        </div>

        {showForm ? (
          <div style={styles.formCard}>
            <input style={styles.input} placeholder="Número de mesa" type="number" value={num}
              autoFocus onChange={e => setNum(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && addMesa()} />
            <div style={styles.formActions}>
              <button style={styles.cancelBtn} onClick={() => setShowForm(false)}>Cancelar</button>
              <button style={styles.saveBtn} onClick={addMesa}>Guardar</button>
            </div>
          </div>
        ) : (
          <button style={styles.addBtn} onClick={() => setShowForm(true)}>+</button>
        )}

        <div style={styles.configCard}>
          <h3 style={styles.configTitle}>Configuración de comandas</h3>

          <div style={styles.configLabel}>Tiempo de tolerancia</div>
          <div style={styles.configRow}>
            <label style={styles.switchLabel}>
              <input type="checkbox" checked={editing} onChange={toggleEdit}
                style={styles.checkbox} />
              <span style={styles.switchText}>{editing ? 'Editando' : 'Bloqueado'}</span>
            </label>
            <input type="number" value={minutos} disabled={!editing}
              onChange={e => setMinutos(Number(e.target.value))}
              min={1} style={{
                ...styles.timeInput,
                opacity: editing ? 1 : 0.5,
                background: editing ? '#fff' : '#f0f0f0',
              }} />
            <span style={styles.unit}>minutos</span>
          </div>

          <div style={{ ...styles.configLabel, marginTop: 16 }}>Intervalo de alerta</div>
          <div style={styles.configRow}>
            <label style={styles.switchLabel}>
              <input type="checkbox" checked={editingAlerta} onChange={toggleEditAlerta}
                style={styles.checkbox} />
              <span style={styles.switchText}>{editingAlerta ? 'Editando' : 'Bloqueado'}</span>
            </label>
            <input type="number" value={intervaloAlerta} disabled={!editingAlerta}
              onChange={e => setIntervaloAlerta(Number(e.target.value))}
              min={0} style={{
                ...styles.timeInput,
                opacity: editingAlerta ? 1 : 0.5,
                background: editingAlerta ? '#fff' : '#f0f0f0',
              }} />
            <span style={styles.unit}>minutos</span>
          </div>
        </div>
      </div>
    </div>
  )
}

const styles = {
  page: { minHeight: '100vh', background: '#f5f5f5' },
  content: { padding: '16px 16px 100px' },
  topRow: { display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 },
  backBtn: {
    width: 36, height: 36, borderRadius: 8, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 20, fontWeight: 700,
    cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
  },
  title: { fontSize: 20, fontWeight: 700, marginBottom: 0 },
  list: { display: 'flex', flexDirection: 'column', gap: 10 },
  card: {
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    background: '#fff', borderRadius: 10, padding: '16px 20px',
    cursor: 'pointer', boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
    fontSize: 20, fontWeight: 600,
  },
  mesaName: { color: '#333' },
  right: { display: 'flex', alignItems: 'center', gap: 10 },
  status: { width: 24, height: 24, borderRadius: '50%', flexShrink: 0 },
  delBtn: {
    width: 28, height: 28, borderRadius: '50%', border: '1px solid #d32f2f',
    color: '#d32f2f', background: '#fff', cursor: 'pointer', fontSize: 14,
    display: 'flex', alignItems: 'center', justifyContent: 'center',
  },
  formCard: {
    background: '#fff', borderRadius: 10, padding: 16, marginTop: 12,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
  },
  input: {
    width: '100%', padding: '12px 14px', borderRadius: 8, border: '1px solid #ddd',
    fontSize: 15, outline: 'none', marginBottom: 10, boxSizing: 'border-box',
  },
  formActions: { display: 'flex', gap: 10 },
  cancelBtn: {
    flex: 1, padding: 11, borderRadius: 8, border: '1px solid #ddd',
    background: '#fff', fontSize: 14, cursor: 'pointer',
  },
  saveBtn: {
    flex: 1, padding: 11, borderRadius: 8, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 14, fontWeight: 600,
    cursor: 'pointer',
  },
  addBtn: {
    width: '100%', padding: 14, borderRadius: 10, border: 'none',
    background: '#d32f2f', color: '#fff', fontSize: 28, fontWeight: 700,
    cursor: 'pointer', marginTop: 12,
  },
  configCard: {
    background: '#fff', borderRadius: 10, padding: 20, marginTop: 20,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
  },
  configTitle: {
    fontSize: 16, fontWeight: 600, color: '#333', marginBottom: 14,
  },
  configLabel: {
    fontSize: 13, fontWeight: 500, color: '#666', marginBottom: 8,
  },
  configRow: {
    display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap',
  },
  switchLabel: {
    display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer',
    userSelect: 'none',
  },
  checkbox: {
    width: 18, height: 18, accentColor: '#d32f2f', cursor: 'pointer',
  },
  switchText: {
    fontSize: 13, color: '#666', fontWeight: 500,
  },
  timeInput: {
    width: 72, padding: '10px 12px', borderRadius: 8, border: '1px solid #ddd',
    fontSize: 15, outline: 'none', textAlign: 'center',
  },
  unit: {
    fontSize: 14, color: '#888',
  },
}
