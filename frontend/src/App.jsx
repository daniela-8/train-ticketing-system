import React, { useState, useEffect } from 'react';
import { ticketingApi } from './api';
import { Train, Search, Clock, AlertCircle, Activity, ShieldCheck, User, CheckCircle2 } from 'lucide-react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import './App.css';

function App() {
    const [activeTab, setActiveTab] = useState('customer');
    const [wsConnected, setWsConnected] = useState(false);
    const [message, setMessage] = useState({ text: '', type: '' });
    const [rides, setRides] = useState([]);

    const showMsg = (text, type) => {
        setMessage({ text, type });
        setTimeout(() => setMessage({ text: '', type: '' }), 5000);
    };

    useEffect(() => {
        let isMounted = true;
        let stompClient = null;

        const connectWebSocket = () => {
            const socket = new SockJS('http://localhost:8080/ws-trains');
            stompClient = Stomp.over(socket);
            stompClient.debug = null;

            stompClient.connect({}, () => {
                if (isMounted) setWsConnected(true);
                stompClient.subscribe('/topic/delays', (msg) => {
                    const update = JSON.parse(msg.body);
                    setRides(prevRides =>
                        prevRides.map(ride =>
                            ride.id === update.rideId
                                ? { ...ride, delayMinutes: update.delayMinutes }
                                : ride
                        )
                    );
                    showMsg(`Ride #${update.rideId} delayed by ${update.delayMinutes} mins`, "warning");
                });
            }, () => {
                if (isMounted) setWsConnected(false);
                setTimeout(connectWebSocket, 5000);
            });
        };

        connectWebSocket();
        return () => {
            isMounted = false;
            if (stompClient?.connected) stompClient.disconnect();
        };
    }, []);

    return (
        <div className="elegant-app-container">
            <header className="elegant-header">
                <div className="brand">
                    <div className="brand-icon-wrapper">
                        <Train size={28} className="brand-icon" />
                    </div>
                    <h1>Train Ticketing System</h1>
                </div>

                <div className="header-controls">
                    <div className="role-toggle">
                        <button className={activeTab === 'customer' ? 'active' : ''} onClick={() => setActiveTab('customer')}>
                            <User size={16} /> Booking
                        </button>
                        <button className={activeTab === 'admin' ? 'active' : ''} onClick={() => setActiveTab('admin')}>
                            <ShieldCheck size={16} /> Admin
                        </button>
                    </div>
                </div>
            </header>

            {message.text && (
                <div className={`toast-message ${message.type}`}>
                    {message.type === 'success' && <CheckCircle2 size={18} />}
                    {message.type === 'warning' && <AlertCircle size={18} />}
                    {message.type === 'error' && <Activity size={18} />}
                    <span>{message.text}</span>
                </div>
            )}

            <main className="main-content">
                {activeTab === 'customer' ?
                    <CustomerPortal rides={rides} setRides={setRides} showMsg={showMsg} /> :
                    <AdminPortal showMsg={showMsg} />
                }
            </main>
        </div>
    );
}

function CustomerPortal({ rides, setRides, showMsg }) {
    const [stations, setStations] = useState([]);
    const [depStation, setDepStation] = useState('');
    const [arrStation, setArrStation] = useState('');
    const [isSearching, setIsSearching] = useState(false);

    useEffect(() => {
        ticketingApi.getStations().then(res => setStations(res.data)).catch(() => showMsg("Unable to load stations", "error"));
    }, []);

    const handleSearch = async () => {
        if (!depStation || !arrStation) return showMsg("Please select both origin and destination.", "warning");
        setIsSearching(true);
        try {
            const res = await ticketingApi.findRoutes(depStation, arrStation);
            setRides(res.data);
            if (res.data.length === 0) showMsg("No direct routes available for this selection.", "warning");
        } catch (err) {
            showMsg(err.response?.data?.message || "Search failed", "error");
        } finally {
            setIsSearching(false);
        }
    };

    return (
        <div className="portal-container animate-fade-in">
            <section className="search-panel">
                <div className="input-group">
                    <label>From</label>
                    <select value={depStation} onChange={e => setDepStation(e.target.value)}>
                        <option value="">Origin station</option>
                        {stations.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                    </select>
                </div>
                <div className="input-group">
                    <label>To</label>
                    <select value={arrStation} onChange={e => setArrStation(e.target.value)}>
                        <option value="">Destination station</option>
                        {stations.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                    </select>
                </div>
                <div className="input-group button-group">
                    <button className="btn-primary" onClick={handleSearch} disabled={isSearching}>
                        {isSearching ? 'Searching...' : <><Search size={18} /> Find Trains</>}
                    </button>
                </div>
            </section>

            <section className="ride-list">
                {rides.map(ride => (
                    <RideCard key={ride.id} ride={ride} showMsg={showMsg} depStation={depStation} arrStation={arrStation} />
                ))}
            </section>
        </div>
    );
}

function RideCard({ ride, showMsg, depStation, arrStation }) {
    const [email, setEmail] = useState('');
    const [seats, setSeats] = useState(1);
    const [isBooking, setIsBooking] = useState(false);

    const handleBook = async () => {
        if (!email) return showMsg("Please provide an email for the ticket receipt.", "warning");
        setIsBooking(true);
        try {
            await ticketingApi.bookTicket({
                userEmail: email, rideId: ride.id,
                departureStationId: depStation, arrivalStationId: arrStation,
                numberOfSeats: parseInt(seats)
            });
            showMsg("Booking confirmed! Your tickets have been emailed.", "success");
            setEmail('');
            setSeats(1);
        } catch (err) {
            showMsg(err.response?.data?.message || "We couldn't complete this booking.", "error");
        } finally {
            setIsBooking(false);
        }
    };

    return (
        <div className="elegant-card">
            <div className="card-header">
                <div className="route-info">
                    <h3>{ride.train?.name || "Express Line"}</h3>
                    <span className="route-tag">{ride.routeName || "Standard Route"}</span>
                </div>
                {ride.delayMinutes > 0 && (
                    <div className="delay-badge">
                        <AlertCircle size={14} /> {ride.delayMinutes}m delay
                    </div>
                )}
            </div>

            <div className="segments-timeline">
                {ride.segments?.map((seg, idx) => (
                    <div key={idx} className="timeline-node">
                        <div className="timeline-locations">
                            <span className="station-name">{seg.fromStation?.name}</span>
                            <span className="arrow">→</span>
                            <span className="station-name">{seg.toStation?.name}</span>
                        </div>
                        <div className="timeline-meta">
                            <span className="meta-item"><Clock size={14} /> {new Date(seg.departureTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                            <span className="meta-divider">•</span>
                            <span className="meta-item">{seg.availableSeats} seats left</span>
                        </div>
                    </div>
                ))}
            </div>

            <div className="booking-controls">
                <input
                    type="email"
                    className="flex-grow"
                    placeholder="Passenger email address"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    disabled={isBooking}
                />
                <div className="seats-wrapper">
                    <label>Seats</label>
                    <input
                        type="number"
                        min="1"
                        value={seats}
                        onChange={e => setSeats(e.target.value)}
                        disabled={isBooking}
                    />
                </div>
                <button className="btn-success" onClick={handleBook} disabled={isBooking}>
                    {isBooking ? 'Processing...' : 'Book Ticket'}
                </button>
            </div>
        </div>
    );
}

function AdminPortal({ showMsg }) {
    const [bookings, setBookings] = useState([]);
    const [delayRideId, setDelayRideId] = useState('');
    const [delayMins, setDelayMins] = useState('');
    const [isReporting, setIsReporting] = useState(false);

    useEffect(() => {
        ticketingApi.getAdminBookings()
            .then(res => setBookings(res.data))
            .catch(() => showMsg("Unable to load booking ledger.", "error"));
    }, []);

    const handleReportDelay = async () => {
        if (!delayRideId || !delayMins) return showMsg("Please provide both Ride ID and delay duration.", "warning");
        setIsReporting(true);
        try {
            await ticketingApi.reportDelay(delayRideId, delayMins);
            showMsg(`Delay broadcasted for Ride #${delayRideId}.`, "success");
            setDelayRideId(''); setDelayMins('');
        } catch (err) {
            showMsg("Failed to broadcast delay.", "error");
        } finally {
            setIsReporting(false);
        }
    };

    return (
        <div className="portal-container animate-fade-in">
            <div className="admin-grid">
                <section className="elegant-card">
                    <div className="card-header border-bottom">
                        <h3>Broadcast Network Delay</h3>
                    </div>
                    <div className="admin-controls">
                        <input type="number" placeholder="Ride ID" value={delayRideId} onChange={e => setDelayRideId(e.target.value)} />
                        <input type="number" placeholder="Minutes delayed" value={delayMins} onChange={e => setDelayMins(e.target.value)} />
                        <button className="btn-primary" onClick={handleReportDelay} disabled={isReporting}>
                            {isReporting ? 'Broadcasting...' : 'Update Status'}
                        </button>
                    </div>
                </section>

                <section className="elegant-card">
                    <div className="card-header border-bottom">
                        <h3>Recent Bookings</h3>
                    </div>
                    <div className="table-container">
                        <table className="elegant-table">
                            <thead>
                            <tr>
                                <th>Ref ID</th>
                                <th>Passenger</th>
                                <th>Ride</th>
                                <th>Qty</th>
                                <th>Status</th>
                            </tr>
                            </thead>
                            <tbody>
                            {bookings.map(b => (
                                <tr key={b.id}>
                                    <td className="font-mono">#{b.id}</td>
                                    <td>{b.userEmail}</td>
                                    <td>#{b.rideId}</td>
                                    <td>{b.numberOfSeats}</td>
                                    <td><span className="status-badge success">Confirmed</span></td>
                                </tr>
                            ))}
                            {bookings.length === 0 && <tr><td colSpan="5" className="empty-state">No recent bookings found.</td></tr>}
                            </tbody>
                        </table>
                    </div>
                </section>
            </div>
        </div>
    );
}

export default App;