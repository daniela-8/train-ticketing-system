import React, { useState, useEffect } from 'react';
import { ticketingApi } from './api';
import { Train, Search, MapPin, Clock, Ticket as TicketIcon, AlertTriangle } from 'lucide-react';

function App() {
    const [stations, setStations] = useState([]);
    const [depStation, setDepStation] = useState('');
    const [arrStation, setArrStation] = useState('');
    const [rides, setRides] = useState([]);
    const [email, setEmail] = useState('');
    const [seats, setSeats] = useState(1);
    const [message, setMessage] = useState({ text: '', type: '' });

    useEffect(() => {
        ticketingApi.getStations()
            .then(res => setStations(res.data))
            .catch(err => showMsg("Failed to load stations", "error"));
    }, []);

    const showMsg = (text, type) => {
        setMessage({ text, type });
        setTimeout(() => setMessage({ text: '', type: '' }), 5000);
    };

    const handleSearch = async () => {
        try {
            const res = await ticketingApi.findRoutes(depStation, arrStation);
            setRides(res.data);
            if (res.data.length === 0) showMsg("No routes found", "warning");
        } catch (err) {
            showMsg(err.response?.data?.message || "Error finding routes", "error");
        }
    };

    const handleBook = async (rideId) => {
        if (!email) return showMsg("Please enter email", "warning");
        try {
            await ticketingApi.bookTicket({
                userEmail: email,
                rideId: rideId,
                departureStationId: depStation,
                arrivalStationId: arrStation,
                numberOfSeats: parseInt(seats)
            });
            showMsg("Ticket Booked Successfully! Check your console for async email log.", "success");
        } catch (err) {
            showMsg(err.response?.data?.message || "Booking failed", "error");
        }
    };

    return (
        <div style={{ fontFamily: 'sans-serif', padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
            <header style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
                <Train size={32} color="#0052cc" />
                <h1 style={{ margin: 0 }}>Siemens Train Link</h1>
            </header>

            {message.text && (
                <div style={{
                    padding: '1rem', marginBottom: '1rem', borderRadius: '4px',
                    backgroundColor: message.type === 'error' ? '#ffebe6' : '#e3fcef',
                    color: message.type === 'error' ? '#bf2600' : '#006644'
                }}>
                    {message.text}
                </div>
            )}

            <section style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', background: '#f4f5f7', padding: '1.5rem', borderRadius: '8px' }}>
                <div>
                    <label><MapPin size={14} /> From</label>
                    <select style={{ width: '100%', padding: '0.5rem' }} value={depStation} onChange={e => setDepStation(e.target.value)}>
                        <option value="">Select Station</option>
                        {stations.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                    </select>
                </div>
                <div>
                    <label><MapPin size={14} /> To</label>
                    <select style={{ width: '100%', padding: '0.5rem' }} value={arrStation} onChange={e => setArrStation(e.target.value)}>
                        <option value="">Select Station</option>
                        {stations.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                    </select>
                </div>
                <button onClick={handleSearch} style={{ gridColumn: 'span 2', padding: '0.75rem', backgroundColor: '#0052cc', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    <Search size={16} /> Search Rides
                </button>
            </section>

            <main style={{ marginTop: '2rem' }}>
                {rides.map(ride => (
                    <div key={ride.id} style={{ border: '1px solid #dfe1e6', borderRadius: '8px', padding: '1rem', marginBottom: '1rem' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <h3>{ride.train.name} <small style={{ color: '#6b778c' }}>({ride.routeName})</small></h3>
                            {ride.delayMinutes > 0 && <span style={{ color: '#de350b', fontWeight: 'bold' }}><AlertTriangle size={14} /> {ride.delayMinutes} min delay</span>}
                        </div>

                        <div style={{ margin: '1rem 0', fontSize: '0.9rem' }}>
                            {ride.segments.map((seg, idx) => (
                                <div key={idx} style={{ padding: '4px 0', borderLeft: '2px solid #0052cc', paddingLeft: '1rem' }}>
                                    <strong>{seg.fromStation.name}</strong> → {seg.toStation.name} <br/>
                                    <small><Clock size={12} /> {new Date(seg.departureTime).toLocaleTimeString()} - Seats: {seg.availableSeats}</small>
                                </div>
                            ))}
                        </div>

                        <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
                            <input type="email" placeholder="Enter Email" value={email} onChange={e => setEmail(e.target.value)} style={{ flex: 2, padding: '0.5rem' }} />
                            <input type="number" min="1" value={seats} onChange={e => setSeats(e.target.value)} style={{ flex: 1, padding: '0.5rem' }} />
                            <button onClick={() => handleBook(ride.id)} style={{ flex: 1, backgroundColor: '#36b37e', color: 'white', border: 'none', padding: '0.5rem', borderRadius: '4px' }}>
                                <TicketIcon size={16} /> Book
                            </button>
                        </div>
                    </div>
                ))}
            </main>
        </div>
    );
}

export default App;