import React, { useState, useEffect } from 'react';
import '../styles/page.css';

const ClickHouseIngestionForm = () => {
  const [sourceType, setSourceType] = useState('ClickHouse');
  const [targetType, setTargetType] = useState('Flat File');
  const [filePath, setFilePath] = useState('');
  const [tableName, setTableName] = useState('');
  const [columnList, setColumnList] = useState([]); // To store columns from the selected table
  const [selectedColumns, setSelectedColumns] = useState([]); // For column selection
  const [status, setStatus] = useState('');
  const [ingestionId, setIngestionId] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [host, setHost] = useState('localhost');
  const [port, setPort] = useState(8123);
  const [database, setDatabase] = useState('uk');
  const [username, setUsername] = useState('default');
  const [password, setPassword] = useState('0702');
  const [resultMessage, setResultMessage] = useState('');
  const [recordCount, setRecordCount] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');
  const [showModal, setShowModal] = useState(false);

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    setStatus('Processing...');
    setIsLoading(true);

    const requestData = {
      sourceType,
      targetType,
      filePath,
      tableName,
      selectedColumns,
      host,
      port,
      database,
      username,
      password,
    };

    try {
      const response = await fetch('http://localhost:8080/api/data/ingest/clickhouse-to-flatfile', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData),
      });

      if (!response.ok) {
        throw new Error('Failed to start ingestion');
      }

      const data = await response.json();
      setIngestionId(data.ingestionId);
      setStatus(data.status);

      if (data.status === 'COMPLETED') {
        setRecordCount(data.recordCount || 0);
        setErrorMessage('');
        setShowModal(true); // Show modal when ingestion is completed
      } else if (data.status === 'FAILED') {
        setErrorMessage(data.errorMessage || 'Unknown error occurred.');
        setRecordCount(null);
      }
    } catch (error) {
      setStatus('Error: ' + error.message);
    } finally {
      setIsLoading(false);
    }
  };

  // Fetch columns for the selected table
  const loadColumns = async () => {
    if (!tableName) {
      setStatus('Please select a table.');
      return;
    }

    setStatus('Loading columns...');
    setIsLoading(true);

    try {
      const response = await fetch(`http://localhost:8080/api/data/tables/${tableName}/columns`);
      if (!response.ok) {
        throw new Error('Failed to load columns.');
      }
      const data = await response.json();
      console.log('Columns response:', data); 
      setColumnList(data); 
      setStatus('Columns loaded.');
    } catch (error) {
      setStatus('Error: ' + error.message);
    } finally {
      setIsLoading(false);
    }
  };

  // Check the status of the ingestion process
  const checkStatus = async () => {
    if (!ingestionId) {
      setStatus('No ingestion process found.');
      return;
    }

    try {
      const response = await fetch(`http://localhost:8080/api/data/ingest/status/${ingestionId}`);
      const data = await response.json();

      if (!response.ok) {
        throw new Error('Failed to retrieve status');
      }

      setStatus(data.status);
    } catch (error) {
      setStatus('Error: ' + error.message);
    }
  };

  useEffect(() => {
    let intervalId;
    if (ingestionId) {
      intervalId = setInterval(() => checkStatus(), 5000); // Poll status every 5 seconds
    }
    return () => clearInterval(intervalId);
  }, [ingestionId]);

  return (
    <div className="form-container">
      <form onSubmit={handleSubmit}>
        <label>Source Type</label>
        <select value={sourceType} onChange={(e) => setSourceType(e.target.value)}>
          <option value="ClickHouse">ClickHouse</option>
          <option value="Flat File">Flat File</option>
        </select>

        <label>Target Type</label>
        <select value={targetType} onChange={(e) => setTargetType(e.target.value)}>
          <option value="ClickHouse">ClickHouse</option>
          <option value="Flat File">Flat File</option>
        </select>

        {sourceType === 'ClickHouse' && (
          <>
            <label>Table Name</label>
            <input type="text" value={tableName} onChange={(e) => setTableName(e.target.value)} />
            <button type="button" onClick={loadColumns} disabled={isLoading}>
              {isLoading ? 'Loading Columns...' : 'Load Columns'}
            </button>

            <div>
              <h4>Select Columns</h4>
              <ul>
                {columnList.length > 0 ? (
                  columnList.map((column, index) => (
                    <li key={index}>
                      <input
                        type="checkbox"
                        value={column}
                        onChange={(e) => {
                          if (e.target.checked) {
                            setSelectedColumns([...selectedColumns, column]);
                          } else {
                            setSelectedColumns(selectedColumns.filter((c) => c !== column));
                          }
                        }}
                      />
                      <label htmlFor={`column-${index}`}>{column}</label>
                    </li>
                  ))
                ) : (
                  <li>No columns available</li>
                )}
              </ul>
            </div>
          </>
        )}

        {targetType === 'Flat File' && (
          <>
            <label>File Path</label>
            <input
              type="text"
              value={filePath}
              onChange={(e) => setFilePath(e.target.value)}
              placeholder="e.g., /tmp/output.csv"
            />
          </>
        )}

        <label>Host</label>
        <input type="text" value={host} onChange={(e) => setHost(e.target.value)} />

        <label>Port</label>
        <input type="number" value={port} onChange={(e) => setPort(parseInt(e.target.value))} />

        <label>Database</label>
        <input type="text" value={database} onChange={(e) => setDatabase(e.target.value)} />

        <label>Username</label>
        <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} />

        <label>Password</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />

        <button type="submit" disabled={isLoading}>
          {isLoading ? 'Ingesting...' : 'Start Ingestion'}
        </button>
      </form>

      {ingestionId && (
        <div>
          <button onClick={checkStatus} disabled={isLoading}>
            {isLoading ? 'Checking Status...' : 'Check Status'}
          </button>
        </div>
      )}

      <div className="status-container">
        <p>Status: {status}</p>

        {status === 'COMPLETED' && (
          <p style={{ color: 'green' }}>
            ✅ Ingestion complete. <strong>{recordCount}</strong> records written to <code>{filePath}</code>.
          </p>
        )}

        {status === 'FAILED' && (
          <p style={{ color: 'red' }}>
            ❌ Ingestion failed: <strong>{errorMessage}</strong>
          </p>
        )}
      </div>

      {showModal && (
        <>
          <div className="overlay" onClick={() => setShowModal(false)}></div>
          <div className="modal">
            <h3>Ingestion Complete</h3>
            <p>{recordCount} records were successfully written to <code>{filePath}</code>.</p>
            <button className="modal-close" onClick={() => setShowModal(false)}>
              Close
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default ClickHouseIngestionForm;
