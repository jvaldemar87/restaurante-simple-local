import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Header from '../../components/Header'
import { categoriasInsumo, categoriasPago, openPdf, reportes, categorias } from '../../api/client'

const meses = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
               'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre']
const anioActual = new Date().getFullYear()

export default function ReporteVentas() {
  const navigate = useNavigate()
  const hoy = new Date().toISOString().split('T')[0]
  const [fechaInicio, setFechaInicio] = useState(
    new Date(Date.now() - 7 * 86400000).toISOString().split('T')[0]
  )
  const [fechaFin, setFechaFin] = useState(hoy)
  const [error, setError] = useState(null)
  const [catsInsumos, setCatsInsumos] = useState([])
  const [catsPagos, setCatsPagos] = useState([])
  const [reportCatInsumos, setReportCatInsumos] = useState('')
  const [reportMesInsumos, setReportMesInsumos] = useState('')
  const [reportAnioInsumos, setReportAnioInsumos] = useState('')
  const [reportCatPagos, setReportCatPagos] = useState('')
  const [reportMesPagos, setReportMesPagos] = useState('')
  const [reportAnioPagos, setReportAnioPagos] = useState('')
  const [fechaPlatillos, setFechaPlatillos] = useState(hoy)
  const [topPlatillos, setTopPlatillos] = useState(10)
  const [anioTendencia, setAnioTendencia] = useState(Number(hoy.split('-')[0]))
  const [anioEstacionalidad, setAnioEstacionalidad] = useState(Number(hoy.split('-')[0]))
  const [topEstacionalidad, setTopEstacionalidad] = useState(10)
  const [vistaEstacionalidad, setVistaEstacionalidad] = useState('mensual')
  const [horaInicioPlatillos, setHoraInicioPlatillos] = useState(0)
  const [horaFinPlatillos, setHoraFinPlatillos] = useState(23)
  const [horaInicioEstacionalidad, setHoraInicioEstacionalidad] = useState(0)
  const [horaFinEstacionalidad, setHoraFinEstacionalidad] = useState(23)
  const [catsMenu, setCatsMenu] = useState([])
  const [catKPIs, setCatKPIs] = useState('')
  const [catPlatillos, setCatPlatillos] = useState('')
  const [catEstacionalidad, setCatEstacionalidad] = useState('')

  useEffect(() => { categoriasInsumo.list().then(setCatsInsumos) }, [])
  useEffect(() => { categoriasPago.list().then(setCatsPagos) }, [])
  useEffect(() => { categorias.list().then(setCatsMenu) }, [])

  const generarVentas = async () => {
    setError(null)
    try {
      await openPdf(`/api/reportes/ventas?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`)
    } catch (e) {
      setError(e.message)
    }
  }

  const generarVentasResumen = async () => {
    setError(null)
    try {
      await openPdf(`/api/reportes/ventas-resumen?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`)
    } catch (e) {
      setError(e.message)
    }
  }

  const generarInsumos = async () => {
    setError(null)
    try {
      let params = ''
      if (reportCatInsumos) params += `categoria=${reportCatInsumos}&`
      if (reportMesInsumos && reportAnioInsumos) params += `mes=${reportMesInsumos}&anio=${reportAnioInsumos}&`
      await openPdf(`/api/reportes/insumos?${params}`)
    } catch (e) {
      setError(e.message)
    }
  }

  const generarPlatillosPorHora = async () => {
    setError(null)
    try {
      await openPdf(`/api/reportes/platillos-por-hora?fecha=${fechaPlatillos}&top=${topPlatillos}`)
    } catch (e) {
      setError(e.message)
    }
  }

  const generarPagos = async () => {
    setError(null)
    try {
      let params = ''
      if (reportCatPagos) params += `categoria=${reportCatPagos}&`
      if (reportMesPagos && reportAnioPagos) params += `mes=${reportMesPagos}&anio=${reportAnioPagos}&`
      await openPdf(`/api/reportes/pagos?${params}`)
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <div style={styles.page}>
      <Header />
      <div style={styles.content}>
        <div style={styles.topRow}>
          <button style={styles.backBtn} onClick={() => navigate('/admin')}>&lt;</button>
          <h2 style={styles.title}>Reportes</h2>
        </div>

        <div style={{ ...styles.card, background: '#eafaf1' }}>
          <h3 style={styles.cardTitle}>Dashboard KPIs</h3>
          <div style={styles.row}>
            <label style={styles.label}>Fecha</label>
            <input type="date" style={styles.input} value={fechaInicio}
              onChange={e => { setFechaInicio(e.target.value); setFechaFin(e.target.value) }} />
          </div>
          <button style={{ ...styles.btn, background: '#1abc9c' }} onClick={async () => {
            setError(null)
            const p = catKPIs ? `fecha=${fechaInicio}&categoria=${catKPIs}` : `fecha=${fechaInicio}`
            try { await openPdf(`/api/reportes/kpis?${p}`) }
            catch (e) { setError(e.message) }
          }}>Generar PDF</button>
          <div style={{ ...styles.row, marginBottom: 0, marginTop: 10 }}>
            <label style={styles.label}>Categoría</label>
            <select style={styles.input} value={catKPIs} onChange={e => setCatKPIs(e.target.value)}>
              <option value="">Todas las categorías</option>
              {catsMenu.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
            </select>
          </div>
        </div>

        <div style={{ ...styles.card, background: '#fdeff2' }}>
          <h3 style={styles.cardTitle}>Horas pico y tráfico</h3>
          <div style={styles.row}>
            <label style={styles.label}>Desde</label>
            <input type="date" style={styles.input} value={fechaInicio}
              onChange={e => setFechaInicio(e.target.value)} />
          </div>
          <div style={styles.row}>
            <label style={styles.label}>Hasta</label>
            <input type="date" style={styles.input} value={fechaFin}
              onChange={e => setFechaFin(e.target.value)} />
          </div>
          <button style={{ ...styles.btn, background: '#c0392b' }} onClick={async () => {
            setError(null)
            try { await openPdf(`/api/reportes/horas-pico?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`) }
            catch (e) { setError(e.message) }
          }}>Generar PDF</button>
        </div>

        <div style={{ ...styles.card, background: '#e8f0fe' }}>
          <h3 style={styles.cardTitle}>Ticket promedio</h3>
          <div style={styles.row}>
            <label style={styles.label}>Desde</label>
            <input type="date" style={styles.input} value={fechaInicio}
              onChange={e => setFechaInicio(e.target.value)} />
          </div>
          <div style={styles.row}>
            <label style={styles.label}>Hasta</label>
            <input type="date" style={styles.input} value={fechaFin}
              onChange={e => setFechaFin(e.target.value)} />
          </div>
          <button style={{ ...styles.btn, background: '#2980b9' }} onClick={async () => {
            setError(null)
            try { await openPdf(`/api/reportes/ticket-promedio?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`) }
            catch (e) { setError(e.message) }
          }}>Generar PDF</button>
        </div>

        <div style={{ ...styles.card, background: '#fce4ec' }}>
          <h3 style={styles.cardTitle}>Tendencia mensual</h3>
          <div style={styles.row}>
            <label style={styles.label}>Año</label>
            <select style={{ ...styles.input, flex: 'none', width: 120 }} value={anioTendencia}
              onChange={e => setAnioTendencia(Number(e.target.value))}>
              {Array.from({ length: 5 }, (_, i) => hoy.split('-')[0] - 2 + i).map(a => (
                <option key={a} value={a}>{a}</option>
              ))}
            </select>
          </div>
          <button style={{ ...styles.btn, background: '#d63384' }} onClick={async () => {
            setError(null)
            try { await openPdf(`/api/reportes/tendencia-mensual?anio=${anioTendencia}`) }
            catch (e) { setError(e.message) }
          }}>Generar PDF</button>
        </div>

        <div style={{ ...styles.card, background: '#ede7f6' }}>
          <h3 style={styles.cardTitle}>Estacionalidad de platillos</h3>
          <div style={styles.row}>
            <label style={styles.label}>Año</label>
            <select style={{ ...styles.input, flex: 'none', width: 120 }} value={anioEstacionalidad}
              onChange={e => setAnioEstacionalidad(Number(e.target.value))}>
              {Array.from({ length: 5 }, (_, i) => Number(hoy.split('-')[0]) - 2 + i).map(a => (
                <option key={a} value={a}>{a}</option>
              ))}
            </select>
          </div>
          <div style={styles.row}>
            <label style={styles.label}>Vista</label>
            <select style={{ ...styles.input, flex: 'none', width: 140 }} value={vistaEstacionalidad}
              onChange={e => setVistaEstacionalidad(e.target.value)}>
              <option value="mensual">Mensual</option>
              <option value="semanal">Semanal</option>
            </select>
          </div>
          <div style={styles.row}>
            <label style={styles.label}>Top</label>
            <input type="number" min={1} max={50} style={{ ...styles.input, width: 80, flex: 'none' }}
              value={topEstacionalidad} onChange={e => setTopEstacionalidad(Number(e.target.value))} />
          </div>
          <button style={{ ...styles.btn, background: '#7c4dff' }} onClick={async () => {
            setError(null)
            let p = `anio=${anioEstacionalidad}&top=${topEstacionalidad}&vista=${vistaEstacionalidad}&horaInicio=${horaInicioEstacionalidad}&horaFin=${horaFinEstacionalidad}`
            if (catEstacionalidad) p += `&categoria=${catEstacionalidad}`
            try { await openPdf(`/api/reportes/estacionalidad?${p}`) }
            catch (e) { setError(e.message) }
          }}>Generar PDF</button>
          <div style={{ ...styles.row, marginBottom: 0, marginTop: 10, gap: 6 }}>
            <label style={{ ...styles.label, width: 30 }}>De</label>
            <input type="number" min={0} max={23} style={{ ...styles.input, width: 50, flex: 'none' }}
              value={horaInicioEstacionalidad} onChange={e => setHoraInicioEstacionalidad(Number(e.target.value))} />
            <label style={{ ...styles.label, width: 25 }}>a</label>
            <input type="number" min={0} max={23} style={{ ...styles.input, width: 50, flex: 'none' }}
              value={horaFinEstacionalidad} onChange={e => setHoraFinEstacionalidad(Number(e.target.value))} />
          </div>
          <div style={{ ...styles.row, marginBottom: 0 }}>
            <label style={styles.label}>Categoría</label>
            <select style={styles.input} value={catEstacionalidad} onChange={e => setCatEstacionalidad(e.target.value)}>
              <option value="">Todas las categorías</option>
              {catsMenu.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
            </select>
          </div>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Reporte de Ventas</h3>
          <div style={styles.row}>
            <label style={styles.label}>Desde</label>
            <input type="date" style={styles.input} value={fechaInicio}
              onChange={e => setFechaInicio(e.target.value)} />
          </div>
          <div style={styles.row}>
            <label style={styles.label}>Hasta</label>
            <input type="date" style={styles.input} value={fechaFin}
              onChange={e => setFechaFin(e.target.value)} />
          </div>
          <button style={{ ...styles.btn, background: '#d32f2f' }} onClick={generarVentas}>Generar PDF</button>
          <button style={{ ...styles.btn, background: '#b71c1c', marginTop: 8 }} onClick={generarVentasResumen}>Generar PDF(Ventas por dia)</button>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Reporte de Insumos</h3>
          <div style={styles.reportRow}>
            <span style={styles.reportLabel}>Reporte:</span>
            <select style={styles.reportSelect} value={reportCatInsumos} onChange={e => setReportCatInsumos(e.target.value)}>
              <option value="">Todas las categorías</option>
              {catsInsumos.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
            </select>
            <select style={styles.reportSelect} value={reportMesInsumos} onChange={e => setReportMesInsumos(e.target.value)}>
              <option value="">Mes</option>
              {meses.map((m, i) => <option key={i} value={i + 1}>{m}</option>)}
            </select>
            <select style={styles.reportSelect} value={reportAnioInsumos} onChange={e => setReportAnioInsumos(e.target.value)}>
              <option value="">Año</option>
              {Array.from({ length: 5 }, (_, i) => anioActual - 2 + i).map(a => <option key={a} value={a}>{a}</option>)}
            </select>
            <button style={styles.reportBtn} onClick={generarInsumos}>PDF</button>
          </div>
        </div>

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Reporte de Pagos</h3>
          <div style={styles.reportRowPagos}>
            <span style={styles.reportLabelPagos}>Reporte:</span>
            <select style={styles.reportSelect} value={reportCatPagos} onChange={e => setReportCatPagos(e.target.value)}>
              <option value="">Todas las categorías</option>
              {catsPagos.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
            </select>
            <select style={styles.reportSelect} value={reportMesPagos} onChange={e => setReportMesPagos(e.target.value)}>
              <option value="">Mes</option>
              {meses.map((m, i) => <option key={i} value={i + 1}>{m}</option>)}
            </select>
            <select style={styles.reportSelect} value={reportAnioPagos} onChange={e => setReportAnioPagos(e.target.value)}>
              <option value="">Año</option>
              {Array.from({ length: 5 }, (_, i) => anioActual - 2 + i).map(a => <option key={a} value={a}>{a}</option>)}
            </select>
            <button style={styles.reportBtnPagos} onClick={generarPagos}>PDF</button>
          </div>
        </div>
        <div style={{ ...styles.card, background: '#fff3e0' }}>
          <h3 style={styles.cardTitle}>Platillos más vendidos por hora</h3>
          <div style={styles.row}>
            <label style={styles.label}>Fecha</label>
            <input type="date" style={styles.input} value={fechaPlatillos}
              onChange={e => setFechaPlatillos(e.target.value)} />
          </div>
          <div style={styles.row}>
            <label style={styles.label}>Top</label>
            <input type="number" min={1} max={50} style={{ ...styles.input, width: 80, flex: 'none' }}
              value={topPlatillos} onChange={e => setTopPlatillos(Number(e.target.value))} />
          </div>
          <button style={{ ...styles.btn, background: '#e67e22' }} onClick={async () => {
            setError(null)
            let p = `fecha=${fechaPlatillos}&top=${topPlatillos}&horaInicio=${horaInicioPlatillos}&horaFin=${horaFinPlatillos}`
            if (catPlatillos) p += `&categoria=${catPlatillos}`
            try { await openPdf(`/api/reportes/platillos-por-hora?${p}`) }
            catch (e) { setError(e.message) }
          }}>Generar PDF</button>
          <div style={{ ...styles.row, marginBottom: 0, marginTop: 10, gap: 6 }}>
            <label style={{ ...styles.label, width: 30 }}>De</label>
            <input type="number" min={0} max={23} style={{ ...styles.input, width: 50, flex: 'none' }}
              value={horaInicioPlatillos} onChange={e => setHoraInicioPlatillos(Number(e.target.value))} />
            <label style={{ ...styles.label, width: 25 }}>a</label>
            <input type="number" min={0} max={23} style={{ ...styles.input, width: 50, flex: 'none' }}
              value={horaFinPlatillos} onChange={e => setHoraFinPlatillos(Number(e.target.value))} />
          </div>
          <div style={{ ...styles.row, marginBottom: 0 }}>
            <label style={styles.label}>Categoría</label>
            <select style={styles.input} value={catPlatillos} onChange={e => setCatPlatillos(e.target.value)}>
              <option value="">Todas las categorías</option>
              {catsMenu.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
            </select>
          </div>
        </div>

        {error && <div style={styles.error}>{error}</div>}
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
  title: { fontSize: 20, fontWeight: 700, marginBottom: 0 },
  card: {
    background: '#fff', borderRadius: 10, padding: 16, marginBottom: 12,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
  },
  cardTitle: { fontSize: 16, fontWeight: 600, marginBottom: 10 },
  desc: { fontSize: 13, color: '#888', marginBottom: 10 },
  row: { display: 'flex', alignItems: 'center', gap: 10, marginBottom: 10 },
  label: { fontSize: 14, color: '#555', width: 60 },
  input: {
    flex: 1, padding: '8px 10px', borderRadius: 8, border: '1px solid #ddd',
    fontSize: 14, outline: 'none',
  },
  btn: {
    width: '100%', padding: 12, borderRadius: 8, border: 'none',
    background: '#f57c00', color: '#fff', fontSize: 14, fontWeight: 600,
    cursor: 'pointer',
  },
  error: {
    background: '#fdecea', color: '#b71c1c', padding: '10px 14px',
    borderRadius: 8, fontSize: 13, marginTop: 8,
  },
  reportRow: {
    display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap',
    padding: '10px 12px', background: '#e3f2fd', borderRadius: 8,
  },
  reportLabel: { fontSize: 13, fontWeight: 600, color: '#1565c0' },
  reportSelect: {
    padding: '6px 8px', borderRadius: 6, border: '1px solid #bbb',
    fontSize: 13, cursor: 'pointer', background: '#fff',
  },
  reportBtn: {
    padding: '6px 14px', borderRadius: 8, border: 'none',
    background: '#1976d2', color: '#fff', fontSize: 13, fontWeight: 600, cursor: 'pointer',
  },
  reportRowPagos: {
    display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap',
    padding: '10px 12px', background: '#e8f5e9', borderRadius: 8,
  },
  reportLabelPagos: { fontSize: 13, fontWeight: 600, color: '#2e7d32' },
  reportBtnPagos: {
    padding: '6px 14px', borderRadius: 8, border: 'none',
    background: '#388e3c', color: '#fff', fontSize: 13, fontWeight: 600, cursor: 'pointer',
  },
}
