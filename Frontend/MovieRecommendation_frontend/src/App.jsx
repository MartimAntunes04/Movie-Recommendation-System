import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import Login from './pages/LoginPage.jsx';
import HomePage from './pages/HomePage.jsx';
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";



function App() {
  return (
     <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/home" element={<HomePage />} />
      </Routes>
    </Router>
  );
}

export default App
