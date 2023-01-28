import * as React from 'react';
import Navbar from "../Navbar/Navbar";
import Button from '@mui/material/Button';
import DialogTitle from '@mui/material/DialogTitle';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import TextField from '@mui/material/TextField';
import Stack from '@mui/material/Stack';
import Grid from '@mui/material/Grid';
import FormControl from '@mui/material/FormControl';
import MenuItem from '@mui/material/MenuItem';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import TextareaAutosize from '@mui/material/TextareaAutosize';
import FormHelperText from '@mui/material/FormHelperText';
import { useEffect } from "react";
import List from '@mui/material/List';
import MyEventItem from "../Utils/MyEventItem";
import { Typography } from "@mui/material";

export default function OrganizedEvents() {

    function createData(eventID, ticketPrice, streetNo, refundPolicy, description, eventType, city, building, neighbourhood, street, district, quota, minimumAge, name, startDate) {
        return { eventID, ticketPrice, streetNo, refundPolicy, description, eventType, city, building, neighbourhood, street, district, quota, minimumAge, name, startDate };
    }
    const [rows, setRows] = React.useState([])
    const [isLoading, setLoading] = React.useState(true);
    useEffect(() => {
        let requestOptions = {
            method: 'GET',
            redirect: 'follow'
        };
        if (isLoading) {
            fetch("http://localhost:8080/getEnrolledEvents?userId=" + localStorage.getItem("userId"), requestOptions)
                .then(response => response.text())
                .then(result => {
                    let resultJSON = JSON.parse(result);
                    if (resultJSON.code === '200') {
                        let curBody = resultJSON.body;

                        for (let curEvent of curBody) {
                            rows.push(createData(curEvent.eventId, curEvent.ticketPrice, curEvent.streetNo, curEvent.refundPolicy, curEvent.description, curEvent.eventType, curEvent.city, curEvent.building, curEvent.neighbourhood, curEvent.street, curEvent.district, curEvent.quota, curEvent.minimumAge, curEvent.eventName, curEvent.startDate));
                            setRows(rows);
                            console.log(rows);
                        }
                        setLoading(false);

                    } else {
                        alert(resultJSON.message)
                    }
                })
                .catch(error => console.log('error', error));
        }

    }, [isLoading]);

    return (
        <>
            <Navbar></Navbar>
            <Typography variant="h6" gutterBottom style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '20px' }}>My Events</Typography>

            {/**CODE FOR ITEMS**/}
            <div>
                <List sx={{ width: '100%' }}>
                    {rows.map((element, index) => {
                        if (index % 3 === 0) return (
                            <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'center', width: '100%', margin: '20px' }}>
                                <>
                                    {index < rows.length && <MyEventItem name={rows[index].name} eventID={rows[index].eventID} city={rows[index].city} district={rows[index].district} neighbourhood={rows[index].neighbourhood} streetNo={rows[index].streetNo} building={rows[index].building} startDate={rows[index].startDate} description={rows[index].description} quota={rows[index].quota} minimumAge={rows[index].minimumAge} eventType={rows[index].eventType} ticketPrice={rows[index].ticketPrice}/>}
                                    {index + 1 < rows.length && <MyEventItem name={rows[index + 1].name} eventID={rows[index + 1].eventID} city={rows[index + 1].city} district={rows[index + 1].district} neighbourhood={rows[index + 1].neighbourhood} streetNo={rows[index + 1].streetNo} building={rows[index + 1].building} startDate={rows[index + 1].startDate} description={rows[index + 1].description} quota={rows[index + 1].quota} minimumAge={rows[index + 1].minimumAge} eventType={rows[index + 1].eventType} ticketPrice={rows[index + 1].ticketPrice}/>}
                                    {index + 2 < rows.length && <MyEventItem name={rows[index + 2].name} eventID={rows[index + 2].eventID} city={rows[index + 2].city} district={rows[index + 2].district} neighbourhood={rows[index + 2].neighbourhood} streetNo={rows[index + 2].streetNo} building={rows[index + 2].building} startDate={rows[index + 2].startDate} description={rows[index + 2].description} quota={rows[index + 2].quota} minimumAge={rows[index + 2].minimumAge} eventType={rows[index + 2].eventType} ticketPrice={rows[index + 2].ticketPrice}/>}

                                </>
                            </Stack>

                        );
                    })}

                </List>
            </div>
        </>
    );
}