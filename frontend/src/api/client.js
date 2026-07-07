import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export async function openPdf(url) {
  const token = localStorage.getItem('token')

  const win = window.open('', '_blank')
  if (!win) {
    const res = await fetch(url, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (!res.ok) {
      const text = await res.text()
      let msg = `Error ${res.status}`
      try { const json = JSON.parse(text); msg = json.error || msg } catch {}
      throw new Error(msg)
    }
    const blob = await res.blob()
    const blobUrl = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = blobUrl
    a.download = url.includes('ventas') ? 'reporte-ventas.pdf'
      : url.includes('insumos') ? 'reporte-insumos.pdf'
      : 'reporte.pdf'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    setTimeout(() => URL.revokeObjectURL(blobUrl), 60000)
    return
  }

  win.document.write('<p style="font-family:sans-serif;padding:2em;color:#555;">Generando reporte...</p>')

  try {
    const res = await fetch(url, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (!res.ok) {
      const text = await res.text()
      let msg = `Error ${res.status}`
      try { const json = JSON.parse(text); msg = json.error || msg } catch {}
      win.close()
      throw new Error(msg)
    }
    const blob = await res.blob()
    const blobUrl = URL.createObjectURL(blob)
    win.location.href = blobUrl
    setTimeout(() => URL.revokeObjectURL(blobUrl), 60000)
  } catch (e) {
    win.close()
    throw e
  }
}

export const auth = {
  login: (username, password) =>
    api.post('/auth/login', { username, password }).then(r => r.data),
}

export const mesas = {
  list: () => api.get('/mesas').then(r => r.data),
  get: (id) => api.get(`/mesas/${id}`).then(r => r.data),
  create: (data) => api.post('/mesas', data).then(r => r.data),
  update: (id, data) => api.put(`/mesas/${id}`, data).then(r => r.data),
  delete: (id) => api.delete(`/mesas/${id}`),
}

export const comensales = {
  listByMesa: (mesaId) => api.get('/comensales', { params: { mesaId } }).then(r => r.data),
  get: (id) => api.get(`/comensales/${id}`).then(r => r.data),
  create: (data) => api.post('/comensales', data).then(r => r.data),
  delete: (id) => api.delete(`/comensales/${id}`),
}

export const pedidos = {
  listByComensal: (comensalId) => api.get('/pedidos', { params: { comensalId } }).then(r => r.data),
  get: (id) => api.get(`/pedidos/${id}`).then(r => r.data),
  create: (comensalId) => api.post('/pedidos', null, { params: { comensalId } }).then(r => r.data),
  addProducto: (pedidoId, data) => api.post(`/pedidos/${pedidoId}/detalles`, data).then(r => r.data),
  removeProducto: (detalleId) => api.delete(`/pedidos/detalles/${detalleId}`),
  cerrar: (id) => api.put(`/pedidos/${id}/cerrar`).then(r => r.data),
  cerrarMesa: (mesaId) => api.post(`/pedidos/cerrar-mesa/${mesaId}`),
  delete: (id) => api.delete(`/pedidos/${id}`),
}

export const categorias = {
  list: () => api.get('/categorias').then(r => r.data),
  create: (data) => api.post('/categorias', data).then(r => r.data),
}

export const productos = {
  list: (categoria, incluirInactivos) => api.get('/productos', { params: { categoria, incluirInactivos } }).then(r => r.data),
  create: (data) => api.post('/productos', data).then(r => r.data),
  update: (id, data) => api.put(`/productos/${id}`, data).then(r => r.data),
  delete: (id) => api.delete(`/productos/${id}`),
  restore: (id) => api.put(`/productos/${id}/restore`).then(r => r.data),
}

export const insumos = {
  list: (categoria, mes, anio) => api.get('/insumos', { params: { categoria, mes, anio } }).then(r => r.data),
  create: (data) => api.post('/insumos', data).then(r => r.data),
  update: (id, data) => api.put(`/insumos/${id}`, data).then(r => r.data),
  delete: (id) => api.delete(`/insumos/${id}`),
}

export const categoriasInsumo = {
  list: () => api.get('/categorias-insumo').then(r => r.data),
}

export const pagos = {
  list: (categoria, mes, anio) => api.get('/pagos', { params: { categoria, mes, anio } }).then(r => r.data),
  create: (data) => api.post('/pagos', data).then(r => r.data),
  update: (id, data) => api.put(`/pagos/${id}`, data).then(r => r.data),
  delete: (id) => api.delete(`/pagos/${id}`),
  uploadImage: (file) => {
    const fd = new FormData()
    fd.append('file', file)
    return api.post('/pagos/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }).then(r => r.data)
  },
}

export const categoriasPago = {
  list: () => api.get('/categorias-pago').then(r => r.data),
}

export const reportes = {
  ticket: (mesaId) => api.get(`/reportes/ticket/${mesaId}`, { responseType: 'blob' }).then(r => r.data),
  comanda: (mesaId) => api.get(`/reportes/comanda/${mesaId}`, { responseType: 'blob' }).then(r => r.data),
  ventas: (fechaInicio, fechaFin) => api.get('/reportes/ventas', { params: { fechaInicio, fechaFin }, responseType: 'blob' }).then(r => r.data),
  insumos: (categoria) => api.get('/reportes/insumos', { params: { categoria }, responseType: 'blob' }).then(r => r.data),
  pagos: (categoria) => api.get('/reportes/pagos', { params: { categoria }, responseType: 'blob' }).then(r => r.data),
}

export default api
