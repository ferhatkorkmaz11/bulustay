import './App.css';
import React from 'react';
import { BrowserRouter, Switch, Route } from "react-router-dom";
//import { BrowserRouter as Router, Route, Switch, withRouter } from "react-router-dom";
//import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'

import Home from './components/Home/Home';
import Login from './components/Login/Login';
import Register from './components/Register/Register';
import RegisterStepTwo from './components/RegisterStepTwo/RegisterStepTwo';
import Events from './components/Events/Events';
import MyProfile from './components/Profiles/MyProfile';
import AdminPanel from './components/AdminPanel/AdminPanel'
import OrganizedEvents from './components/OrganizedEvents/OrganizedEvents';
import MyEvents from './components/MyEvents/MyEvents'
import OrganizerProfile from './components/Profiles/OrganizerProfile';
import Favorites from './components/Favorites/Favorites'
import "./App.css";


function App() {
  
  
  return (
    <div className="bg_image">
      <BrowserRouter>
        <>
          <Switch>
            <Route exact path="/" component={Login} />
            <Route exact path="/register" component={Register} />
            <Route exact path="/registersteptwo" component={RegisterStepTwo} />
            <Route exact path="/home" component={Home} />
            <Route exact path="/events" component={Events} />
            <Route exact path="/favorites" component={Favorites} />
            <Route exact path="/myprofile" component={MyProfile} />
            <Route exact path="/adminpanel" component={AdminPanel} />
            <Route exact path="/organizedevents" component={OrganizedEvents} />
            <Route exact path="/myevents" component={MyEvents} />
            <Route exact path="/organizerprofile" component={OrganizerProfile} />

          </Switch>
        </>
      </BrowserRouter>
    </div>
  )
}


export default App;