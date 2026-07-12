import { useState, useEffect, useRef, useCallback, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { cocina, configuracion } from '../../api/client'
import Header from '../../components/Header'

const ROTATIONS = {}

function getRotation(pedidoId) {
  if (!ROTATIONS[pedidoId]) {
    ROTATIONS[pedidoId] = (Math.random() * 4 - 2).toFixed(1)
  }
  return ROTATIONS[pedidoId]
}

function playAlertBeepTriple() {
  for (let i = 0; i < 3; i++) {
    setTimeout(() => {
      try {
        const ctx = new (window.AudioContext || window.webkitAudioContext)()
        const osc = ctx.createOscillator()
        const gain = ctx.createGain()
        osc.connect(gain)
        gain.connect(ctx.destination)
        osc.type = 'square'
        osc.frequency.value = 880
        gain.gain.value = 0.2
        osc.start()
        osc.stop(ctx.currentTime + 0.12)
      } catch (e) {
      }
    }, i * 300)
  }
}

export default function CocinaView() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [comandas, setComandas] = useState([])
  const [tiempoTolerancia, setTiempoTolerancia] = useState(30)
  const [alertaIntervalo, setAlertaIntervalo] = useState(5)
  const prevUrgentes = useRef(new Set())
  const [entregando, setEntregando] = useState(null)

  const isAdmin = user?.rol === 'ADMIN'

  const loadData = useCallback(async () => {
    try {
      const data = await cocina.comandas()
      setComandas(data)
    } catch (e) {
      console.error('Error cargando comandas', e)
    }
    try {
      const config = await configuracion.getTiempoTolerancia()
      setTiempoTolerancia(config.minutos)
    } catch (e) {
      console.error('Error cargando config', e)
    }
    try {
      const alerta = await configuracion.getAlertaIntervalo()
      setAlertaIntervalo(alerta.minutos)
    } catch (e) {
      console.error('Error cargando alerta intervalo', e)
    }
  }, [])

  useEffect(() => {
    loadData()
    const interval = setInterval(loadData, 12000)
    return () => clearInterval(interval)
  }, [loadData])

  const comandasProcesadas = useMemo(() => {
    const ahora = new Date()
    return comandas.map(c => {
      const fechaComanda = new Date(c.fechaComanda)
      const diffMs = ahora - fechaComanda
      const diffMin = diffMs / 60000
      const urgente = diffMin > tiempoTolerancia
      return { ...c, urgente, diffMin }
    })
    .sort((a, b) => {
      if (a.urgente && !b.urgente) return -1
      if (!a.urgente && b.urgente) return 1
      if (a.urgente && b.urgente) return b.diffMin - a.diffMin
      return b.diffMin - a.diffMin
    })
  }, [comandas, tiempoTolerancia])

  useEffect(() => {
    const urgentesAhora = new Set(
      comandasProcesadas.filter(c => c.urgente).map(c => c.pedidoId)
    )
    let newUrgent = false
    urgentesAhora.forEach(id => {
      if (!prevUrgentes.current.has(id)) {
        newUrgent = true
      }
    })
    if (newUrgent) {
      playAlertBeepTriple()
    }
    prevUrgentes.current = urgentesAhora
  }, [comandasProcesadas])

  useEffect(() => {
    if (!alertaIntervalo || alertaIntervalo < 1) return
    const interval = setInterval(() => {
      const hayUrgentes = comandasProcesadas.some(c => c.urgente)
      if (hayUrgentes) playAlertBeepTriple()
    }, alertaIntervalo * 60000)
    return () => clearInterval(interval)
  }, [alertaIntervalo, comandasProcesadas])

  const handleEntregar = async (pedidoId, e) => {
    e.stopPropagation()
    if (entregando) return
    setEntregando(pedidoId)
    try {
      await cocina.entregar(pedidoId)
      setComandas(prev => prev.filter(c => c.pedidoId !== pedidoId))
    } catch (err) {
      console.error('Error al entregar comanda', err)
    } finally {
      setEntregando(null)
    }
  }

  const handleBack = () => {
    if (isAdmin) {
      navigate('/admin')
    }
  }

  const minEspera = (diffMin) => {
    if (diffMin < 1) return 'Ahora'
    const mins = Math.floor(diffMin)
    if (mins < 60) return `${mins} min`
    const hrs = Math.floor(mins / 60)
    const resto = mins % 60
    return `${hrs}h ${resto}m`
  }

  return (
    <div style={styles.page}>
      <Header />

      <div style={styles.toolbar}>
        {isAdmin && (
          <button style={styles.backBtn} onClick={handleBack}>&lt;</button>
        )}
        <h2 style={styles.title}>Cocina</h2>
        <div style={styles.toolbarRight}>
          <span style={styles.tolerancia}>Tol: {tiempoTolerancia} min</span>
          <span style={styles.contador}>
            {comandas.length} comanda{comandas.length !== 1 ? 's' : ''}
          </span>
        </div>
      </div>

      <div style={styles.grid}>
        {comandasProcesadas.length === 0 && (
          <div style={styles.empty}>
            <div style={styles.emptyIcon}>&#128161;</div>
            <p>Sin comandas pendientes</p>
            <p style={styles.emptySub}>Los pedidos enviados a cocina aparecerán aquí</p>
          </div>
        )}

        {comandasProcesadas.map(c => {
          const urgente = c.urgente
          const rot = getRotation(c.pedidoId)

          return (
            <div
              key={c.pedidoId}
              style={{
                ...styles.card,
                background: urgente ? '#ffcdd2' : '#fff9c4',
                borderColor: urgente ? '#d32f2f' : '#f9a825',
                transform: `rotate(${rot}deg)`,
                animation: 'none',
              }}
            >
              {urgente && <div style={styles.urgentBadge}>URGENTE</div>}

              <div style={styles.cardHeader}>
                <span style={styles.mesaNum}>Mesa {c.mesaNumero}</span>
                <span style={styles.cardTime}>{minEspera(c.diffMin)}</span>
              </div>

              <div style={styles.comensalName}>{c.comensalNombre}</div>

              <div style={styles.divider} />

              <div style={styles.itemsList}>
                {c.items.map((item, idx) => (
                  <div key={idx} style={styles.itemRow}>
                    <span style={styles.itemQty}>- {item.cantidadTotal}x</span>
                    <span style={styles.itemName}>{item.productoNombre}</span>
                  </div>
                ))}
              </div>

              <button
                style={{
                  ...styles.entregarBtn,
                  opacity: entregando === c.pedidoId ? 0.5 : 1,
                }}
                onClick={(e) => handleEntregar(c.pedidoId, e)}
                disabled={entregando === c.pedidoId}
              >
                {entregando === c.pedidoId ? '...' : 'X'}
              </button>
            </div>
          )
        })}
      </div>
    </div>
  )
}

const styles = {
  page: {
    minHeight: '100vh',
    background: '#fafafa',
    display: 'flex',
    flexDirection: 'column',
  },
  toolbar: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    padding: '12px 20px',
    background: '#fff',
    borderBottom: '1px solid #e0e0e0',
    position: 'sticky',
    top: 0,
    zIndex: 10,
  },
  backBtn: {
    width: 36,
    height: 36,
    borderRadius: 8,
    border: 'none',
    background: '#d32f2f',
    color: '#fff',
    fontSize: 20,
    fontWeight: 700,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  title: {
    fontSize: 20,
    fontWeight: 700,
    flex: 1,
  },
  toolbarRight: {
    display: 'flex',
    alignItems: 'center',
    gap: 12,
    flexShrink: 0,
  },
  tolerancia: {
    fontSize: 12,
    color: '#888',
    background: '#f5f5f5',
    padding: '4px 8px',
    borderRadius: 4,
  },
  contador: {
    fontSize: 14,
    fontWeight: 600,
    color: '#333',
    background: '#f5f5f5',
    padding: '4px 10px',
    borderRadius: 4,
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, 280px)',
    gap: 24,
    justifyContent: 'center',
    padding: '24px 20px 40px',
    flex: 1,
    overflowY: 'auto',
  },
  empty: {
    gridColumn: '1 / -1',
    textAlign: 'center',
    padding: '60px 20px',
    color: '#999',
  },
  emptyIcon: {
    fontSize: 48,
    marginBottom: 12,
  },
  emptySub: {
    fontSize: 13,
    marginTop: 4,
    color: '#bbb',
  },
  card: {
    width: 280,
    minHeight: 200,
    borderRadius: 4,
    padding: '20px 18px 16px',
    position: 'relative',
    boxShadow: '2px 3px 10px rgba(0,0,0,0.12), 1px 1px 3px rgba(0,0,0,0.08)',
    display: 'flex',
    flexDirection: 'column',
    transition: 'transform 0.15s ease, box-shadow 0.15s ease',
    cursor: 'default',
    border: '2px solid',
  },
  urgentBadge: {
    position: 'absolute',
    top: -12,
    right: -12,
    background: '#d32f2f',
    color: '#fff',
    fontSize: 11,
    fontWeight: 800,
    padding: '4px 10px',
    borderRadius: 4,
    boxShadow: '1px 2px 4px rgba(0,0,0,0.2)',
    letterSpacing: 0.5,
  },
  cardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 2,
  },
  mesaNum: {
    fontSize: 18,
    fontWeight: 800,
    color: '#333',
  },
  cardTime: {
    fontSize: 12,
    color: '#888',
    fontWeight: 500,
  },
  comensalName: {
    fontSize: 15,
    fontWeight: 600,
    color: '#555',
    marginBottom: 8,
  },
  divider: {
    height: 1,
    background: 'rgba(0,0,0,0.1)',
    marginBottom: 10,
  },
  itemsList: {
    flex: 1,
    marginBottom: 12,
  },
  itemRow: {
    display: 'flex',
    gap: 4,
    fontSize: 14,
    lineHeight: '1.6',
    alignItems: 'baseline',
  },
  itemQty: {
    fontWeight: 700,
    color: '#333',
    whiteSpace: 'nowrap',
  },
  itemName: {
    color: '#444',
    wordBreak: 'break-word',
  },
  entregarBtn: {
    alignSelf: 'center',
    width: 52,
    height: 52,
    borderRadius: '50%',
    border: '3px solid #d32f2f',
    background: '#fff',
    color: '#d32f2f',
    fontSize: 24,
    fontWeight: 800,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    transition: 'all 0.15s ease',
    marginTop: 4,
  },
}
