import React from 'react';
import './Navbar.css';
import { useParams, withRouter, useHistory } from 'react-router-dom';

export default function Navbar() {
  const history = useHistory();

  function Logout(){
    localStorage.removeItem("userId")
    localStorage.removeItem("email")
    history.push("/")
  }
  return (
    <nav className="navbar">
      <ul>
        <li><a href="#" onClick={() => { history.push("/events") }}>Events</a></li>
        <li><a href="#" onClick={() => { history.push("/myevents") }}>My Events</a></li>
        <li><a href="#" onClick={() => { history.push("/organizedevents") }}>Organized Events</a></li>
        <li><a href="#" onClick={() => { history.push("/favorites") }}>Favorites</a></li>
        <li><a href="#" onClick={() => { history.push("/myprofile") }}>My Profile</a></li>
        <li><a href="#" onClick={Logout}>Logout</a></li>
      </ul>
    </nav>
  );
}