import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Header from '../../components/Header'
import { respaldo } from '../../api/client'

export default function Respaldos() {
  const navigate = useNavigate()
  const [incluirImagenes, setIncluirImagenes] = useState(false)
  const [exportando, setExportando] = useState(false)
  const [importando, setImportando] = useState(false)
  const [archivo, setArchivo] = useState(null)
  const [mensaje, setMensaje] = useState(null)
  const [error, setError] = useState(null)

  const handleExportar = async () => {
    setExportando(true)
    setError(null)
    setMensaje(null)
    try {
      await respaldo.exportar(incluirImagenes)
      setMensaje('Respaldo exportado exitosamente.')
    } catch (e) {
      setError(e.message || 'Error al exportar el respaldo.')
    } finally {
      setExportando(false)
    }
  }

  const handleImportar = async () => {
    if (!archivo) {
      setError('Selecciona un archivo ZIP para importar.')
      return
    }
    setImportando(true)
    setError(null)
    setMensaje(null)
    try {
      const data = await respaldo.importar(archivo)
      setMensaje(data.mensaje || 'Datos importados exitosamente.')
      setArchivo(null)
      const fileInput = document.getElementById('import-file')
      if (fileInput) fileInput.value = ''
    } catch (e) {
      const msg = e.response?.data?.error || e.message || 'Error al importar el respaldo.'
      setError(msg)
    } finally {
      setImportando(false)
    }
  }

  return (
    <div style={styles.page}>
      <Header />
      <div style={styles.content}>
        <button style={styles.backBtn} onClick={() => navigate('/admin')}>&lt;</button>
        <h2 style={styles.title}>Respaldos</h2>

        {mensaje && <div style={styles.success}>{mensaje}</div>}
        {error && <div style={styles.error}>{error}</div>}

        <div style={styles.grid}>

          <div style={{ ...styles.card, borderTop: '4px solid #7b1fa2' }}>
            <span style={styles.cardTitle}>Exportar Datos</span>
            <p style={styles.cardDesc}>
              Descarga un archivo ZIP con todos los datos del sistema.
            </p>
            <label style={styles.checkbox}>
              <input
                type="checkbox"
                checked={incluirImagenes}
                onChange={e => setIncluirImagenes(e.target.checked)}
              />
              <span>Incluir imágenes de evidencia</span>
            </label>
            <button
              style={styles.btn}
              onClick={handleExportar}
              disabled={exportando}
            >
              {exportando ? 'Exportando...' : 'Exportar'}
            </button>
          </div>

          <div style={{ ...styles.card, borderTop: '4px solid #c62828' }}>
            <span style={styles.cardTitle}>Importar Datos</span>
            <p style={styles.cardDesc}>
              Restaura los datos desde un archivo ZIP previamente exportado.
            </p>
            <input
              id="import-file"
              type="file"
              accept=".zip"
              style={styles.fileInput}
              onChange={e => setArchivo(e.target.files[0])}
            />
            <button
              style={{ ...styles.btn, background: '#c62828' }}
              onClick={handleImportar}
              disabled={importando || !archivo}
            >
              {importando ? 'Importando...' : 'Importar'}
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
  backBtn: {
    background: '#d32f2f', color: '#fff', border: 'none',
    borderRadius: 20, width: 36, height: 36, fontSize: 18,
    fontWeight: 700, cursor: 'pointer', marginBottom: 12,
    display: 'flex', alignItems: 'center', justifyContent: 'center',
  },
  title: { fontSize: 20, fontWeight: 700, marginBottom: 16 },
  success: {
    background: '#e8f5e9', color: '#2e7d32', padding: '10px 14px',
    borderRadius: 8, fontSize: 14, marginBottom: 12,
  },
  error: {
    background: '#ffebee', color: '#c62828', padding: '10px 14px',
    borderRadius: 8, fontSize: 14, marginBottom: 12,
  },
  grid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 },
  card: {
    background: '#fff', borderRadius: 10, padding: 24,
    display: 'flex', flexDirection: 'column', gap: 12,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
  },
  cardTitle: { fontSize: 16, fontWeight: 700, color: '#333' },
  cardDesc: { fontSize: 13, color: '#777', margin: 0 },
  checkbox: {
    display: 'flex', alignItems: 'center', gap: 8,
    fontSize: 14, color: '#555', cursor: 'pointer',
  },
  fileInput: { fontSize: 13 },
  btn: {
    background: '#7b1fa2', color: '#fff', border: 'none',
    borderRadius: 8, padding: '10px 0', fontSize: 14,
    fontWeight: 600, cursor: 'pointer', textAlign: 'center',
  },
}
