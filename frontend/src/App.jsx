import React, { useState, useEffect } from 'react';
import { ticketingApi } from './api';
import { Train, Search, MapPin, Clock, Ticket as TicketIcon, AlertTriangle, Radio } from 'lucide-react';

import SockJS from 'sockjs-client';
import Stomp from 'stompjs';


function App() {
    const [stations, setStations] = useState([]);
    const [depStation, setDepStation] = useState('');
    const [arrStation, setArrStation] = useState('');
    const [rides, setRides] = useState([]);
    const [email, setEmail] = useState('');
    const [seats, setSeats] = useState(1);
    const [message, setMessage] = useState({ text: '', type: '' });
    const [wsConnected, setWsConnected] = useState(false);

    useEffect(() => {
        let isMounted = true;
        let stompClient = null;

        const connectWebSocket = () => {
            const socket = new SockJS('http://localhost:8080/ws-trains');
            stompClient = Stomp.over(socket);
            stompClient.debug = null;

            stompClient.connect({}, () => {
                if (isMounted) {
                    setWsConnected(true);
                    console.log("Connected to Siemens Live Delay Stream");

                    stompClient.subscribe('/topic/delays', (msg) => {
                        const update = JSON.parse(msg.body);
                        setRides(prevRides =>
                            prevRides.map(ride =>
                                ride.id === update.rideId
                                    ? { ...ride, delayMinutes: update.delayMinutes }
                                    : ride
                            )
                        );
                        showMsg(`Real-time Update: Ride #${update.rideId} delayed by ${update.delayMinutes} mins`, "warning");
                    });
                } else {
                    stompClient.disconnect();
                }
            }, (error) => {
                console.log("WebSocket connection attempt failed. Retrying...");
                if (isMounted) setWsConnected(false);
            });
        };

        connectWebSocket();

        return () => {
            isMounted = false;
            if (stompClient && stompClient.connected) {
                stompClient.disconnect();
            }
        };
    }, []);

    useEffect(() => {
        ticketingApi.getStations()
            .then(res => setStations(res.data))
            .catch(() => showMsg("Backend Offline", "error"));
    }, []);

    const showMsg = (text, type) => {
        setMessage({ text, type });
        setTimeout(() => setMessage({ text: '', type: '' }), 6000);
    };

    const handleSearch = async () => {
        try {
            const res = await ticketingApi.findRoutes(depStation, arrStation);
            setRides(res.data);
            if (res.data.length === 0) showMsg("No direct routes available", "warning");
        } catch (err) {
            showMsg(err.response?.data?.message || "Search failed", "error");
        }
    };

    const handleBook = async (rideId) => {
        if (!email) return showMsg("Email required for confirmation", "warning");
        try {
            await ticketingApi.bookTicket({
                userEmail: email, rideId,
                departureStationId: depStation,
                arrivalStationId: arrStation,
                numberOfSeats: parseInt(seats)
            });
            showMsg("Ticket Booked! Async confirmation email triggered.", "success");
        } catch (err) {
            showMsg(err.response?.data?.message || "Booking conflict", "error");
        }
    };

    return (
        <div style={{ fontFamily: 'Inter, system-ui, sans-serif', padding: '2rem', maxWidth: '850px', margin: '0 auto', color: '#172b4d' }}>
            <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <Train size={32} color="#0052cc" />
                    <h1 style={{ margin: 0, fontSize: '1.5rem' }}>Siemens TrainLink</h1>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.8rem', color: wsConnected ? '#36b37e' : '#ff5630' }}>
                    <Radio size={14} className={wsConnected ? 'pulse' : ''} />
                    {wsConnected ? 'LIVE UPDATES ACTIVE' : 'RECONNECTING TO SERVER...'}
                </div>
            </header>

            {message.text && (
                <div style={{
                    padding: '1rem', marginBottom: '1.5rem', borderRadius: '6px', borderLeft: '4px solid',
                    backgroundColor: message.type === 'error' ? '#ffebe6' : message.type === 'warning' ? '#fffae6' : '#e3fcef',
                    borderColor: message.type === 'error' ? '#ff5630' : message.type === 'warning' ? '#ffab00' : '#36b37e'
                }}>
                    {message.text}
                </div>
            )}

            {/* Search Controls */}
            <section style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', background: '#f4f5f7', padding: '1.5rem', borderRadius: '12px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold', fontSize: '0.9rem' }}>Departure</label>
                    <select style={{ width: '100%', padding: '0.6rem', borderRadius: '4px', border: '1px solid #ddd' }} value={depStation} onChange={e => setDepStation(e.target.value)}>
                        <option value="">Choose Start...</option>
                        {stations.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                    </select>
                </div>
                <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold', fontSize: '0.9rem' }}>Destination</label>
                    <select style={{ width: '100%', padding: '0.6rem', borderRadius: '4px', border: '1px solid #ddd' }} value={arrStation} onChange={e => setArrStation(e.target.value)}>
                        <option value="">Choose End...</option>
                        {stations.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                    </select>
                </div>
                <button onClick={handleSearch} style={{ gridColumn: 'span 2', padding: '0.8rem', backgroundColor: '#0052cc', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.5rem' }}>
                    <Search size={18} /> SEARCH AVAILABLE RIDES
                </button>
            </section>

            {/* Ride List */}
            <main style={{ marginTop: '2.5rem' }}>
                {rides.map(ride => (
                    <div key={ride.id} style={{ border: '1px solid #ebecf0', borderRadius: '10px', padding: '1.5rem', marginBottom: '1.5rem', transition: 'all 0.3s' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <div>
                                <h3 style={{ margin: '0 0 0.5rem 0' }}>{ride.train.name}</h3>
                                <span style={{ color: '#0052cc', background: '#deebff', padding: '2px 8px', borderRadius: '4px', fontSize: '0.8rem' }}>{ride.routeName}</span>
                            </div>
                            {ride.delayMinutes > 0 && (
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#de350b', background: '#ffebe6', padding: '4px 12px', borderRadius: '20px', fontWeight: 'bold', fontSize: '0.85rem' }}>
                                    <AlertTriangle size={16} /> {ride.delayMinutes} MIN DELAY
                                </div>
                            )}
                        </div>

                        <div style={{ margin: '1.5rem 0' }}>
                            {ride.segments.map((seg, idx) => (
                                <div key={idx} style={{ padding: '8px 0', borderLeft: '2px solid #dfe1e6', paddingLeft: '1.5rem', position: 'relative' }}>
                                    <div style={{ position: 'absolute', left: '-5px', top: '15px', width: '8px', height: '8px', borderRadius: '50%', background: '#0052cc' }}></div>
                                    <strong style={{ fontSize: '0.95rem' }}>{seg.fromStation.name}</strong> → {seg.toStation.name} <br/>
                                    <small style={{ color: '#6b778c' }}><Clock size={12} /> Departs: {new Date(seg.departureTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})} | Seats: {seg.availableSeats}</small>
                                </div>
                            ))}
                        </div>

                        <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.5rem', paddingTop: '1rem', borderTop: '1px solid #f4f5f7' }}>
                            <input type="email" placeholder="Customer Email" value={email} onChange={e => setEmail(e.target.value)} style={{ flex: 2, padding: '0.6rem', borderRadius: '4px', border: '1px solid #ddd' }} />
                            <input type="number" min="1" value={seats} onChange={e => setSeats(e.target.value)} style={{ width: '70px', padding: '0.6rem', borderRadius: '4px', border: '1px solid #ddd' }} />
                            <button onClick={() => handleBook(ride.id)} style={{ flex: 1, backgroundColor: '#36b37e', color: 'white', border: 'none', padding: '0.6rem', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' }}>
                                CONFIRM BOOKING
                            </button>
                        </div>
                    </div>
                ))}
            </main>
        </div>
    );
}

export default App;