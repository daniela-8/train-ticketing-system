import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
});

export const ticketingApi = {
    getStations: () => api.get('/stations'),
    findRoutes: (depId, arrId) => api.get(`/routes?departureId=${depId}&arrivalId=${arrId}`),
    bookTicket: (bookingData) => api.post('/tickets/book', bookingData),
    getAdminBookings: () => api.get('/admin/bookings'),
    reportDelay: (rideId, minutes) => api.post(`/admin/rides/${rideId}/delay?minutes=${minutes}`),
};

export default api;