import React, { useEffect } from "react";
import List from '@mui/material/List';
import Navbar from "../Navbar/Navbar";
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import FilterAltIcon from '@mui/icons-material/FilterAlt';
import IconButton from '@mui/material/IconButton';
import TextField from '@mui/material/TextField';
import DialogTitle from '@mui/material/DialogTitle';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Select from '@mui/material/Select';
import { LocalizationProvider } from '@mui/x-date-pickers-pro';
import { AdapterDayjs } from '@mui/x-date-pickers-pro/AdapterDayjs';
import FormGroup from '@mui/material/FormGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import EventItem from "../Utils/EventItem";
import { Typography } from "@mui/material";

export default function Events() {
    function createData(eventID, ticketPrice, streetNo, refundPolicy, description, eventType, city, building, neighbourhood, street, district, quota, minimumAge, name, startDate, isFavorited, organizerId) {
        return { eventID, ticketPrice, streetNo, refundPolicy, description, eventType, city, building, neighbourhood, street, district, quota, minimumAge, name, startDate, isFavorited, organizerId };
    }
    const [rows, setRows] = React.useState([])
    const [isLoading, setLoading] = React.useState(true);
    useEffect(() => {
        let requestOptions = {
            method: 'GET',
            redirect: 'follow'
        };
        if (isLoading) {
            fetch("http://localhost:8080/getAllEvents?userId=" + localStorage.getItem("userId"), requestOptions)
                .then(response => response.text())
                .then(result => {
                    let resultJSON = JSON.parse(result);
                    if (resultJSON.code === '200') {
                        let curBody = resultJSON.body;

                        for (let curEvent of curBody) {
                            rows.push(createData(curEvent.eventId, curEvent.ticketPrice, curEvent.streetNo, curEvent.refundPolicy, curEvent.description, curEvent.eventType, curEvent.city, curEvent.building, curEvent.neighbourhood, curEvent.street, curEvent.district, curEvent.quota, curEvent.minimumAge, curEvent.eventName, curEvent.startDate, curEvent.isFavorited, curEvent.organizerId));
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

    const [eventType, setEventType] = React.useState('');
    const [minPrice, setMinPrice] = React.useState('');
    const [maxPrice, setMaxPrice] = React.useState('');
    const [quotaRes, setQuotaRes] = React.useState(false);
    const [city, setCity] = React.useState('');
    const [minAge, setMinAge] = React.useState('');
    const [open, setOpen] = React.useState(false);
    const [startDate, setStartDate] = React.useState(new Date());
    const [endDate, setEndDate] = React.useState(new Date());
    const [search, setSearch] = React.useState('');

    const handleEventType = (event) => {
        setEventType(event.target.value);
    };
    const handleMinPrice = (event) => {
        setMinPrice(event.target.value);
    };
    const handleMaxPrice = (event) => {
        setMaxPrice(event.target.value);
    };
    const handleQuotaRes = (event) => {
        setQuotaRes(event.target.checked);
    };
    const handleCity = (event) => {
        setCity(event.target.value);
    };
    const handleMinAge = (event) => {
        setMinAge(event.target.value);
    };
    const handleClickOpen = (position) => {
        setOpen(true);
    };
    const handleClose = () => {
        setOpen(false);
    };
    const handleStartDate = (event) => {
        setStartDate(event);
    };
    const handleEndDate = (event) => {
        setEndDate(event);
    };
    const handleSearch = (event) => {
        setSearch(event.target.value);
    };

    const filterItems = () => {
        console.log(eventType, quotaRes, city, minAge, minPrice, maxPrice, startDate, endDate);/** SOME OF THEM MIGHT BE NULL!!!!!**/
        var requestOptions = {
            method: 'GET',
            redirect: 'follow'
        };
        let query;
        let startDateStr = startDate?.toISOString().slice(0, 19).replace('T', ' ')
        if (startDateStr === undefined) { startDateStr = "" }
        let endDateStr = endDate?.toISOString().slice(0, 19).replace('T', ' ')
        if (endDateStr === undefined) { endDateStr = "" }
        if (minAge === '') {

            query = "http://localhost:8080/getFilteredEvents?eventType=" + eventType + "&city=" + city + "&minAge=0&minPrice=" + minPrice + "&maxPrice=" + maxPrice + "&minDate=" + startDateStr + "&maxDate=" + endDateStr + "&onlyAvailable=" + quotaRes + "&userId=" + localStorage.getItem("userId");
        }
        else {
            query = "http://localhost:8080/getFilteredEvents?eventType=" + eventType + "&city=" + city + "&minAge=" + minAge + "&minPrice=" + minPrice + "&maxPrice=" + maxPrice + "&minDate=" + startDateStr + "&maxDate=" + endDateStr + "&onlyAvailable=" + quotaRes + "&userId=" + localStorage.getItem("userId");

        }
        console.log("ferhat query is: " + query)
        fetch(query, requestOptions)
            .then(response => response.text())
            .then(result => {
                let responseJSON = JSON.parse(result);
                if (responseJSON.code !== '200') {
                    alert(responseJSON.message);
                } else {
                    let newRow = []
                    let responseBody = responseJSON.body;
                    for (let curEvent of responseBody) {
                        newRow.push(createData(curEvent.eventId, curEvent.ticketPrice, curEvent.streetNo, curEvent.refundPolicy, curEvent.description, curEvent.eventType, curEvent.city, curEvent.building, curEvent.neighbourhood, curEvent.street, curEvent.district, curEvent.quota, curEvent.minimumAge, curEvent.eventName, curEvent.startDate));
                        console.log(newRow);
                    }
                    setRows(newRow);
                }
            })
            .catch(error => console.log('error', error));

    }

    const searchItems = (event) => {
        if (event.keyCode === 13) {/**
         YAZACAĞINIZ KOD IF'İN İÇİNDE OLSUN!!!!**/
            console.log(search);
            let requestOptions = {
                method: 'GET',
                redirect: 'follow'
            };

            fetch("http://localhost:8080/getEventsWithName?eventName=" + event.target.value + "&userId=" + localStorage.getItem("userId"), requestOptions)
                .then(response => response.text())
                .then(result => {
                    let responseJSON = JSON.parse(result);
                    if (responseJSON.code !== '200') {
                        alert(responseJSON.message);
                    } else {
                        let newRow = []
                        let responseBody = responseJSON.body;
                        for (let curEvent of responseBody) {
                            newRow.push(createData(curEvent.eventId, curEvent.ticketPrice, curEvent.streetNo, curEvent.refundPolicy, curEvent.description, curEvent.eventType, curEvent.city, curEvent.building, curEvent.neighbourhood, curEvent.street, curEvent.district, curEvent.quota, curEvent.minimumAge, curEvent.eventName, curEvent.startDate));
                            console.log(newRow);
                        }
                        setRows(newRow);
                    }
                })
                .catch(error => console.log('error', error));
        }

    }

    document.body.style = 'background: #F4F9F9;';



    setTimeout(() => {
        // or wait for 100ms until orders state updated

    }, 1000);

    console.log(rows.length)
    return (

        <>
            <Navbar />
            <Typography variant="h6" gutterBottom style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '20px' }}>Events</Typography>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '10px' }}>
                <TextField id="outlined-basic" onChange={handleSearch} onKeyDown={searchItems} label="Search Events" variant="outlined" sx={{ m: 1, minWidth: 150 }} />
                <IconButton aria-label="Filter" onClick={handleClickOpen}>
                    <FilterAltIcon fontSize="large" />
                </IconButton>
                <Dialog open={open} onClose={handleClose}  >
                    <DialogTitle>Filter Events </DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            Please specify your choices.
                        </DialogContentText>
                        <div>
                            <FormControl sx={{ m: 1, minWidth: 150 }}>
                                <InputLabel id="dropdown-event-type">Event Type</InputLabel>
                                <Select
                                    labelId="dropdown-event-type"
                                    id="dropdown-event"
                                    value={eventType}
                                    onChange={handleEventType}
                                    label="Event Type"
                                >
                                    <MenuItem value="">
                                        <em>None</em>
                                    </MenuItem>
                                    <MenuItem value={"Sport"}>Sport</MenuItem>
                                    <MenuItem value={"Music"}>Music</MenuItem>
                                    <MenuItem value={"Party"}>Party</MenuItem>
                                    <MenuItem value={"Art"}>Art</MenuItem>
                                    <MenuItem value={"Gathering"}>Gathering</MenuItem>
                                    <MenuItem value={"Movie"}>Movie</MenuItem>
                                    <MenuItem value={"Technology"}>Technology</MenuItem>

                                </Select>
                            </FormControl>
                        </div>
                        <div>
                            <FormControl sx={{ m: 1, minWidth: 150 }}>

                                <InputLabel id="dropdown-event-city" sx={{ display: 'inline-block' }}>City</InputLabel>
                                <Select
                                    labelId="dropdown-event-city"
                                    id="dropdown-city"
                                    value={city}
                                    onChange={handleCity}
                                    autoWidth
                                    label="City"
                                >
                                    <MenuItem value="">
                                        <em>None</em>
                                    </MenuItem>
                                    <MenuItem value={"Ankara"}>Ankara</MenuItem>
                                    <MenuItem value={"Istanbul"}>Istanbul</MenuItem>
                                    <MenuItem value={"Konya"}>Konya</MenuItem>
                                    <MenuItem value={"Hatay"}>Hatay</MenuItem>
                                    <MenuItem value={"Adana"}>Adana</MenuItem>
                                    <MenuItem value={"Balikesir"}>Balikesir</MenuItem>
                                </Select>
                            </FormControl>

                        </div>
                        <div>
                            <FormControl sx={{ m: 1, minWidth: 150 }}>

                                <InputLabel id="dropdown-min-age" sx={{ display: 'inline-block' }}>Minimum Age</InputLabel>
                                <Select
                                    labelId="dropdown-min-age"
                                    id="dropdown-age"
                                    value={minAge}
                                    onChange={handleMinAge}
                                    autoWidth
                                    label="Min Age"
                                >
                                    <MenuItem value="">
                                        <em>None</em>
                                    </MenuItem>
                                    <MenuItem value={"3"}>+3</MenuItem>
                                    <MenuItem value={"6"}>+6</MenuItem>
                                    <MenuItem value={"13"}>+13</MenuItem>
                                    <MenuItem value={"16"}>+16</MenuItem>
                                    <MenuItem value={"18"}>+18</MenuItem>
                                </Select>
                            </FormControl>

                        </div>
                        <Stack direction="row">
                            <TextField id="outlined-basic" value={minPrice} onChange={handleMinPrice} label="Minimum Price" variant="outlined" type="number" sx={{ m: 1, minWidth: 150 }} />
                            <TextField id="outlined-basic" value={maxPrice} onChange={handleMaxPrice} label="Maximum Price" variant="outlined" type="number" sx={{ m: 1, minWidth: 150 }} />
                        </Stack>
                        <LocalizationProvider dateAdapter={AdapterDayjs}>
                            <DateTimePicker
                                label="Start Date"
                                value={startDate}
                                onChange={handleStartDate}
                                renderInput={(params) => <TextField {...params} />}
                            />
                        </LocalizationProvider>
                        <LocalizationProvider dateAdapter={AdapterDayjs}>
                            <DateTimePicker
                                label="End Date"
                                value={endDate}
                                onChange={handleEndDate}
                                renderInput={(params) => <TextField {...params} />}
                            />
                        </LocalizationProvider>


                        <FormGroup>
                            <FormControlLabel control={<Checkbox checked={quotaRes} onChange={handleQuotaRes} />} label="Show Only Events with Available Quota" />
                        </FormGroup>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => {
                            setOpen(false)
                        }} >Cancel </Button>
                        <Button onClick={() => {
                            filterItems(); setOpen(false)
                        }} >Submit</Button>
                    </DialogActions>
                </Dialog>

            </div>

            <div>
                <List sx={{ width: '100%' }}>
                    {rows.map((element, index) => {
                        if (index % 3 === 0) return (
                            <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'center', width: '100%', margin: '20px' }}>
                                <>
                                    {index < rows.length && <EventItem name={rows[index].name} eventID={rows[index].eventID} city={rows[index].city} district={rows[index].district} neighbourhood={rows[index].neighbourhood} streetNo={rows[index].streetNo} building={rows[index].building} startDate={rows[index].startDate} description={rows[index].description} quota={rows[index].quota} minimumAge={rows[index].minimumAge} eventType={rows[index].eventType} ticketPrice={rows[index].ticketPrice} isFavorited={rows[index].isFavorited} organizerID = {rows[index].organizerId}/>}
                                    {index + 1 < rows.length && <EventItem name={rows[index + 1].name} eventID={rows[index + 1].eventID} city={rows[index + 1].city} district={rows[index + 1].district} neighbourhood={rows[index + 1].neighbourhood} streetNo={rows[index + 1].streetNo} building={rows[index + 1].building} startDate={rows[index + 1].startDate} description={rows[index + 1].description} quota={rows[index + 1].quota} minimumAge={rows[index + 1].minimumAge} eventType={rows[index + 1].eventType} ticketPrice={rows[index + 1].ticketPrice} isFavorited={rows[index + 1].isFavorited} organizerID = {rows[index + 1].organizerId}/>}
                                    {index + 2 < rows.length && <EventItem name={rows[index + 2].name} eventID={rows[index + 2].eventID} city={rows[index + 2].city} district={rows[index + 2].district} neighbourhood={rows[index + 2].neighbourhood} streetNo={rows[index + 2].streetNo} building={rows[index + 2].building} startDate={rows[index + 2].startDate} description={rows[index + 2].description} quota={rows[index + 2].quota} minimumAge={rows[index + 2].minimumAge} eventType={rows[index + 2].eventType} ticketPrice={rows[index + 2].ticketPrice} isFavorited={rows[index + 2].isFavorited} organizerID = {rows[index + 2].organizerId}/>}
                                </>
                            </Stack>

                        );
                    })}

                </List>
            </div>
        </>
    )


}
