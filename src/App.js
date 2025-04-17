import React from 'react';
import ClickHouseIngestionForm from './components/ClickHouseIngestionForm';

function App() {
  return (
    <div className="App" style={{ fontFamily: 'Arial, sans-serif', padding: '20px' }}>
      <h1 style={{ textAlign: 'center', color: '#00796b' }}> Bidirectional ClickHouse & Flat File Data 
      Ingestion Tool</h1>
      <ClickHouseIngestionForm />
    </div>
  );
}

export default App;
