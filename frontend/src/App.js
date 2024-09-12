import React, { useState } from 'react';
import axios from 'axios';  
import { Container, Row, Col, Alert, ListGroup } from 'react-bootstrap';
import UserForm from './components/userform';  

const App = () => {
    const [message, setMessage] = useState('');
    const [messageType, setMessageType] = useState('success');
    const [users, setUsers] = useState([]);

    const handleFormSubmit = async (userData) => {
        try {
            const response = await axios.post('http://backend-service:5000/users', userData);
            setUsers(prevUsers => [...prevUsers, response.data]);
            setMessage('User detail submitted successfully!');
            setMessageType('success');
        } catch (err) {
            setMessage('Failed to submit user details.');
            setMessageType('danger');
        }
    };

    return (
        <Container className="mt-5">
            <Row>
                <Col md={{ span: 6, offset: 3 }}>
                    <h1 className="text-center mb-4">Devops Jobs Registration</h1>
                    {message && <Alert variant={messageType} className="text-center">{message}</Alert>}
                    <UserForm onSubmit={handleFormSubmit} />
                    <h2 className="mt-5">Registered Users</h2>
                    <ListGroup>
                        {users.map(user => (
                            <ListGroup.Item key={user._id}>
                                <strong>Name:</strong> {user.name} <br />
                                <strong>Phone:</strong> {user.phone} <br />
                                <strong>Email:</strong> {user.email}
                            </ListGroup.Item>
                        ))}
                    </ListGroup>
                </Col>
            </Row>
        </Container>
    );
};

export default App;
