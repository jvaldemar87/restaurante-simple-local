import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/auth/Login'
import ListaMesas from './pages/mesero/ListaMesas'
import ComensalesMesa from './pages/mesero/ComensalesMesa'
import DetalleComensal from './pages/mesero/DetalleComensal'
import MenuProductos from './pages/mesero/MenuProductos'
import ResumenOrden from './pages/mesero/ResumenOrden'
import ListaMesasCajero from './pages/cajero/ListaMesas'
import ComensalesMesaCajero from './pages/cajero/ComensalesMesa'
import DetalleComensalCajero from './pages/cajero/DetalleComensal'
import CocinaView from './pages/cocinero/CocinaView'
import Dashboard from './pages/admin/Dashboard'
import GestionMesas from './pages/admin/GestionMesas'
import GestionMenu from './pages/admin/GestionMenu'
import GestionInsumos from './pages/admin/GestionInsumos'
import GestionPagos from './pages/admin/GestionPagos'
import ReporteVentas from './pages/admin/ReporteVentas'

function HomeRedirect() {
  const { isAuthenticated, user, loading } = useAuth()

  if (loading) return null

  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (user?.rol) return <Navigate to={`/${user.rol.toLowerCase()}`} replace />
  return <Navigate to="/login" replace />
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<HomeRedirect />} />

        <Route path="/mesero" element={<ProtectedRoute roles={['MESERO','ADMIN']}><ListaMesas /></ProtectedRoute>} />
        <Route path="/mesero/mesa/:mesaId" element={<ProtectedRoute roles={['MESERO','ADMIN']}><ComensalesMesa /></ProtectedRoute>} />
        <Route path="/mesero/mesa/:mesaId/comensal/:comensalId" element={<ProtectedRoute roles={['MESERO','ADMIN']}><DetalleComensal /></ProtectedRoute>} />
        <Route path="/mesero/mesa/:mesaId/comensal/:comensalId/menu" element={<ProtectedRoute roles={['MESERO','ADMIN']}><MenuProductos /></ProtectedRoute>} />
        <Route path="/mesero/mesa/:mesaId/comensal/:comensalId/resumen" element={<ProtectedRoute roles={['MESERO','ADMIN']}><ResumenOrden /></ProtectedRoute>} />

        <Route path="/cajero" element={<ProtectedRoute roles={['CAJERO','ADMIN']}><ListaMesasCajero /></ProtectedRoute>} />
        <Route path="/cajero/mesa/:mesaId" element={<ProtectedRoute roles={['CAJERO','ADMIN']}><ComensalesMesaCajero /></ProtectedRoute>} />
        <Route path="/cajero/mesa/:mesaId/comensal/:comensalId" element={<ProtectedRoute roles={['CAJERO','ADMIN']}><DetalleComensalCajero /></ProtectedRoute>} />

        <Route path="/cocinero" element={<ProtectedRoute roles={['COCINERO','ADMIN']}><CocinaView /></ProtectedRoute>} />

        <Route path="/admin" element={<ProtectedRoute roles={['ADMIN']}><Dashboard /></ProtectedRoute>} />
        <Route path="/admin/mesas" element={<ProtectedRoute roles={['ADMIN']}><GestionMesas /></ProtectedRoute>} />
        <Route path="/admin/mesas/mesa/:mesaId" element={<ProtectedRoute roles={['ADMIN']}><ComensalesMesa /></ProtectedRoute>} />
        <Route path="/admin/mesas/mesa/:mesaId/comensal/:comensalId" element={<ProtectedRoute roles={['ADMIN']}><DetalleComensal /></ProtectedRoute>} />
        <Route path="/admin/mesas/mesa/:mesaId/comensal/:comensalId/menu" element={<ProtectedRoute roles={['ADMIN']}><MenuProductos /></ProtectedRoute>} />
        <Route path="/admin/mesas/mesa/:mesaId/comensal/:comensalId/resumen" element={<ProtectedRoute roles={['ADMIN']}><ResumenOrden /></ProtectedRoute>} />
        <Route path="/admin/menu" element={<ProtectedRoute roles={['ADMIN']}><GestionMenu /></ProtectedRoute>} />
        <Route path="/admin/insumos" element={<ProtectedRoute roles={['ADMIN']}><GestionInsumos /></ProtectedRoute>} />
        <Route path="/admin/pagos" element={<ProtectedRoute roles={['ADMIN']}><GestionPagos /></ProtectedRoute>} />
        <Route path="/admin/reportes" element={<ProtectedRoute roles={['ADMIN']}><ReporteVentas /></ProtectedRoute>} />
        <Route path="/admin/cocina" element={<ProtectedRoute roles={['ADMIN']}><CocinaView /></ProtectedRoute>} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  )
}
