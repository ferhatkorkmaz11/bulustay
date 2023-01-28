import * as React from 'react';
import Button from '@mui/material/Button';
import DialogTitle from '@mui/material/DialogTitle';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import Stack from '@mui/material/Stack';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import FormControl from '@mui/material/FormControl';
import InputAdornment from '@mui/material/InputAdornment';
import Input from '@mui/material/Input';
import FormHelperText from '@mui/material/FormHelperText';
import ListItem from '@mui/material/ListItem';
import Divider from '@mui/material/Divider';
import ListItemText from '@mui/material/ListItemText';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import TextareaAutosize from '@mui/material/TextareaAutosize';
import TextField from '@mui/material/TextField';
import "./Item.css"

export default function OrgEventItem(props) {
    const [eventId, setEventId] = React.useState(props.eventID);
    const [eventCity, setEventCity] = React.useState(props.city);
    const [eventName, setEventName] = React.useState(props.name);
    const [eventType, setEventType] = React.useState(props.eventType);
    const [eventDescription, setEventDescription] = React.useState(props.description);
    const [eventDate, setEventDate] = React.useState(props.startDate);
    const [eventDistrict, setEventDistrict] = React.useState(props.district);
    const [eventNeighbourhood, setEventNeighbourhood] = React.useState(props.neighbourhood);
    const [eventStreet, setEventStreet] = React.useState(props.street);
    const [eventStreetNo, setEventStreetNo] = React.useState(props.streetNo);
    const [eventRefundPolicy, setEventRefundPolicy] = React.useState(props.refundPolicy);
    const [discountPercentage, setDiscountPercentage] = React.useState(props.discountPercentage);

    const [eventTicketPrice, setEventTicketPrice] = React.useState(props.ticketPrice);


    const [eventBuilding, setEventBuilding] = React.useState(props.building);

    const [minAge, setMinAge] = React.useState(props.minimumAge);
    const [eventQuota, setEventQuota] = React.useState(props.quota);

    const [amount, setDiscountAmount] = React.useState('')
    const [open2, setOpen2] = React.useState(false)
    const [open3, setOpen3] = React.useState(false)
    const [open4, setOpen4] = React.useState(false)
    const [rows, setRows] = React.useState([])
    const [open10, setOpen10] = React.useState(false);

    const handleOpen10 = () => {
        setOpen10(true);
    };

    const handleClose10 = () => {
        setOpen10(false);
    };



    const handleClickOpen2 = (position) => {
        setOpen2(true)
    };
    const handleClose2 = () => {
        setDiscountAmount('')
        setOpen2(false)
    };


    const handleClickOpen3 = (position) => {
        setOpen3(true)
    };
    const handleClose3 = () => {
        setOpen3(false)
    };
    const handleClickOpen4 = (position) => {
        getParticipants();
        setOpen4(true)
    };
    const handleClose4 = () => {
        setOpen4(false)
    };
    const handleCityChange = (event) => {
        setEventCity(event.target.value);
    };

    const handleEventTypeChange = (event) => {
        setEventType(event.target.value);
    };

    const handleMinAgeChange = (event) => {
        setMinAge(event.target.value);
    };


    const cancelChanges = () => {

        setEventCity(props.city)
        setEventName(props.name)
        setEventType(props.eventType)
        setEventDescription(props.description)
        setEventDate(props.startDate)
        setEventDistrict(props.district)
        setEventNeighbourhood(props.neighbourhood)
        setEventStreet(props.street)
        setEventStreetNo(props.streetNo)
        setEventBuilding(props.building)
        setMinAge(props.minimumAge)
        setEventTicketPrice(props.ticketPrice)
        setEventQuota(props.quota)
        setEventRefundPolicy(props.refundPolicy)

    }
    function createData(participantName) {

        return { participantName };
    }


    const getParticipants = () => {
        setRows([])
        var requestOptions = {
            method: 'GET',
            redirect: 'follow'

        };

        fetch("http://localhost:8080/getEnrolledParticipants?eventId=" + eventId, requestOptions)
            .then(response => response.text())
            .then(result => {
                let responseJSON = JSON.parse(result);
                let newRows = []
                if (responseJSON.code !== '200') {
                    alert(responseJSON.message);
                } else {
                    let responseBody = responseJSON.body;
                    for (let curEvent of responseBody) {
                        newRows.push(createData(curEvent.name));
                    }
                    setRows(newRows)
                }
            })
            .catch(error => console.log('error', error));

    }





    const cancelDiscount = () => {
        var requestOptions = {
            method: 'DELETE',
            redirect: 'follow'

        };

        fetch("http://localhost:8080/deleteDiscount?eventId=" + eventId, requestOptions)
            .then(response => response.text())
            .then(result => {
                window.location.reload(false);
            })
            .catch(error => console.log('error', error));

      

    }
    const submitChanges = () => {


        var requestOptions = {
            method: 'PATCH',
            redirect: 'follow'
        };

        fetch("http://localhost:8080/editEvent?eventId=" + eventId + "&eventName=" + eventName + "&description=" + eventDescription + "&ticketPrice=" + eventTicketPrice + "&quota=" + eventQuota + "&eventType=" + eventType + "&refundPolicy" + eventRefundPolicy, requestOptions)
            .then(response => response.text())
            .then(result => console.log(result))
            .catch(error => console.log('error', error));

        handleClose3();
        console.log(eventRefundPolicy);
        window.location.reload(false);
    }
    const applyDiscount = () => {
        let myHeaders = new Headers();
        myHeaders.append("Content-Type", "application/json");

        let raw = JSON.stringify({
            "eventId": props.eventID,
            "discountPercentage": parseInt(amount),
            "organizerId": parseInt(localStorage.getItem("userId"))
        });
        console.log(raw);
        var requestOptions = {
            method: 'POST',
            headers: myHeaders,
            body: raw,
            redirect: 'follow'
        };

        fetch("http://localhost:8080/createDiscount", requestOptions)
            .then(response => response.text())
            .then(result => {
                let resultJSON = JSON.parse(result);
                if (resultJSON.code === '200') {
                    window.location.reload(false);
                }
                else {
                    alert(resultJSON.message)
                }
            })
            .catch(error => console.log('error', error));
    }

    const cancelEvent = () => {
        let requestOptions = {
            method: 'DELETE',
            redirect: 'follow'
        };

        fetch("http://localhost:8080/deleteEvent?eventId=" + props.eventID, requestOptions)
            .then(response => response.text())
            .then(result => {
                let resultJSON = JSON.parse(result);
                if (resultJSON.code === '200') {
                    window.location.reload(false);
                }
                else {
                    alert(resultJSON.message)
                }
            })
            .catch(error => console.log('error', error));
            handleClose10()
    }


    return (
        <>
            <ListItem key={props.eventID} className="list-item" sx={{ width: '300px' }}>
                <Stack direction="column">
                    <ListItemText
                        primary={props.name}
                        secondary={
                            <React.Fragment>
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Event Location: {props.city}, {props.district}, {props.neighbourhood} {props.building}, {props.street},{props.streetNo}
                                </Typography>
                                <br />
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Start Date: {props.startDate}
                                </Typography>
                                <br />
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Ticket Price: {props.ticketPrice}
                                </Typography>
                                <br />
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Description: {props.description}
                                </Typography>
                                <br />
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Quota: {props.quota}
                                </Typography>
                                <br />
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Min Age: {props.minimumAge}
                                </Typography>
                                <br />
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Event Type: {props.eventType}
                                </Typography>
                            </React.Fragment>
                        }
                    />
                    {discountPercentage ?
                        <Button
                            type="submit"
                            variant="contained"
                            onClick={cancelDiscount}
                            sx={{ mt: 1, mb: 2, background: "#92B4EC" }}
                        >
                            Cancel Discount
                        </Button>
                        :
                        <Button
                            type="submit"
                            variant="contained"
                            onClick={handleClickOpen2}
                            sx={{ mt: 1, mb: 2, background: "#92B4EC" }}
                        >
                            Apply Discount
                        </Button>
                    }
                    <Button
                        type="submit"
                        variant="contained"
                        onClick={handleClickOpen3}
                        sx={{ mt: 1, mb: 2, background: "#92B4EC" }}
                    >
                        Edit Event
                    </Button>
                    <Button
                        type="submit"
                        variant="contained"
                        onClick={handleOpen10}
                        sx={{ mt: 1, mb: 2, background: "#92B4EC" }}
                    >
                        Cancel Event
                    </Button>
                        <Dialog
                            open={open10}
                            onClose={handleClose10}
                            aria-labelledby="alert-dialog-title"
                            aria-describedby="alert-dialog-description"
                        >
                            <DialogTitle id="alert-dialog-title">
                                {"Cancel Event?"}
                            </DialogTitle>
                            <DialogContent>
                                <DialogContentText id="alert-dialog-description">
                                    Are you sure you want to cancel this event?
                                </DialogContentText>
                            </DialogContent>
                            <DialogActions>
                                <Button onClick={handleClose10}>No</Button>
                                <Button onClick={cancelEvent} autoFocus>
                                    Yes
                                </Button>
                            </DialogActions>
                        </Dialog>
                    <Button
                        type="submit"
                        variant="contained"

                        onClick={() => { getParticipants(); handleClickOpen4() }}
                        sx={{ mt: 1, mb: 2, background: "#92B4EC" }}
                    >
                        Show Participants
                    </Button>
                    <Dialog open={open2} onClose={handleClose2}  >
                        <DialogTitle>Discount</DialogTitle>
                        <DialogContent>
                            <DialogContentText>
                            </DialogContentText>
                            <Stack direction="column" sx={{ width: '80%', margin: '30px' }} justifyContent="center" >

                                <React.Fragment>

                                    <Grid container spacing={3}>

                                        <Grid item xs={12} sm={6}>
                                            <FormControl variant="standard" sx={{ m: 1, mt: 3, width: '25ch' }}>
                                                <Input
                                                    required
                                                    id="amount"
                                                    endAdornment={<InputAdornment position="end">%</InputAdornment>}
                                                    onChange={event => setDiscountAmount(event.target.value)}
                                                    aria-describedby="amount"
                                                    type='number'
                                                    inputProps={{
                                                        'aria-label': 'amount',
                                                    }}
                                                    label="Discount Amount"
                                                />
                                                <FormHelperText id="amount-helper-text">Discount Amount</FormHelperText>
                                            </FormControl>
                                        </Grid>
                                    </Grid>
                                </React.Fragment>
                            </Stack>
                        </DialogContent>
                        <DialogActions>
                            <Button onClick={handleClose2} >Cancel </Button>
                            <Button onClick={() => {
                                applyDiscount();
                            }} >Apply</Button>
                        </DialogActions>
                    </Dialog>
                    <Dialog open={open3} onClose={handleClose3}  >
                        <DialogTitle>Edit Event</DialogTitle>
                        <DialogContent>
                            <DialogContentText>
                            </DialogContentText>
                            <Stack direction="column" sx={{ width: '80%', margin: '30px' }} justifyContent="center" >

                                <React.Fragment>

                                    <Grid container spacing={3}>
                                        <Grid item xs={12} sm={6}>
                                            <TextField onChange={event => setEventName(event.target.value)}
                                                id="eventName"
                                                name="eventName"
                                                label="Event name"
                                                value={eventName}
                                                fullWidth
                                                autoComplete="eventName"
                                                variant="standard"
                                            />
                                        </Grid>
                                        <Grid item xs={12} sm={6}>

                                            <FormControl sx={{ m: 1, minWidth: 150 }}>
                                                <InputLabel id="dropdown-event-type">Event Type</InputLabel>
                                                <Select
                                                    labelId="dropdown-event-type"
                                                    id="dropdown-event"
                                                    value={eventType}
                                                    onChange={handleEventTypeChange}
                                                    label="Event Type"
                                                >
                                                    <MenuItem value={"Sport"}>Sport</MenuItem>
                                                    <MenuItem value={"Music"}>Music</MenuItem>
                                                    <MenuItem value={"Party"}>Party</MenuItem>
                                                    <MenuItem value={"Art"}>Art</MenuItem>
                                                    <MenuItem value={"Gathering"}>Gathering</MenuItem>
                                                    <MenuItem value={"Movie"}>Movie</MenuItem>
                                                    <MenuItem value={"Technology"}>Technology</MenuItem>


                                                </Select>
                                            </FormControl>

                                        </Grid>

                                        <Grid item xs={12} sm={6}>

                                            <FormControl sx={{ m: 1, minWidth: 150 }}>

                                                <InputLabel id="dropdown-event-city" sx={{ display: 'inline-block' }}>City</InputLabel>
                                                <Select
                                                    disabled
                                                    labelId="dropdown-event-city"
                                                    id="dropdown-city"
                                                    value={props.city}
                                                    onChange={handleCityChange}
                                                    autoWidth
                                                    label="City"
                                                >
                                                    <MenuItem value={"Ankara"}>Ankara</MenuItem>
                                                    <MenuItem value={"Istanbul"}>Istanbul</MenuItem>
                                                    <MenuItem value={"Konya"}>Konya</MenuItem>
                                                </Select>
                                            </FormControl>

                                        </Grid>

                                        <Grid item xs={12} sm={6}>
                                            <TextField onChange={event => setEventDistrict(event.target.value)}
                                                disabled
                                                id="district"
                                                name="district"
                                                value={eventDistrict}
                                                label="District"
                                                fullWidth
                                                variant="standard"
                                            />
                                        </Grid>
                                        <Grid item xs={12} sm={6}>
                                            <TextField onChange={event => setEventNeighbourhood(event.target.value)}
                                                disabled
                                                id="neighbourhood"
                                                name="neighbourhood"
                                                value={eventNeighbourhood}
                                                label="Neighbourhood"
                                                fullWidth
                                                variant="standard"
                                            />
                                        </Grid>

                                        <Grid item xs={12} sm={6}>
                                            <TextField onChange={event => setEventStreet(event.target.value)}
                                                disabled
                                                id="street"
                                                name="street"
                                                value={eventStreet}
                                                label="Street"
                                                fullWidth
                                                variant="standard"
                                            />
                                        </Grid>

                                        <Grid item xs={12} sm={6}>
                                            <TextField onChange={event => setEventStreetNo(event.target.value)}
                                                disabled
                                                id="streetNo"
                                                name="streetNo"
                                                value={eventStreetNo}
                                                label="Street No"
                                                fullWidth
                                                variant="standard"
                                            />
                                        </Grid>

                                        <Grid item xs={12} sm={6}>
                                            <TextField onChange={event => setEventBuilding(event.target.value)}
                                                disabled
                                                id="building"
                                                name="building"
                                                label="Building"
                                                value={eventBuilding}
                                                fullWidth
                                                autoComplete="building"
                                                variant="standard"
                                            />
                                        </Grid>
                                        <Grid item xs={12} md={6}>
                                            <LocalizationProvider dateAdapter={AdapterDateFns}>
                                                <DateTimePicker
                                                    disabled
                                                    label="Event Date"
                                                    value={eventDate}
                                                    onChange={(newValue) => {
                                                        setEventDate(newValue);
                                                    }}
                                                    renderInput={(params) => <TextField {...params} />}
                                                />
                                            </LocalizationProvider>
                                        </Grid>

                                        <Grid item xs={12} sm={6}>
                                            <TextField onChange={event => setEventQuota(event.target.value)}
                                                id="quota"
                                                name="quota"
                                                label="Event Quota"
                                                value={eventQuota}
                                                fullWidth
                                                autoComplete="Event Quota"
                                                type="number" sx={{ m: 1, minWidth: 150 }}
                                                variant="standard"
                                            />
                                        </Grid>
                                        <Grid item xs={12} sm={6}>
                                            <FormControl sx={{ m: 1, minWidth: 150 }}>

                                                <InputLabel id="dropdown-min-age" sx={{ display: 'inline-block' }}>Minimum Age</InputLabel>
                                                <Select
                                                    disabled
                                                    labelId="dropdown-min-age"
                                                    id="dropdown-age"
                                                    value={minAge}
                                                    onChange={handleMinAgeChange}
                                                    autoWidth
                                                    label="Min Age"
                                                >

                                                    <MenuItem value={""}>N/A</MenuItem>
                                                    <MenuItem value={"3"}>+3</MenuItem>
                                                    <MenuItem value={"6"}>+6</MenuItem>
                                                    <MenuItem value={"13"}>+13</MenuItem>
                                                    <MenuItem value={"16"}>+16</MenuItem>
                                                    <MenuItem value={"18"}>+18</MenuItem>
                                                </Select>
                                            </FormControl>

                                        </Grid>
                                        <Grid item xs={12} sm={6}>
                                            <TextField onChange={event => setEventTicketPrice(event.target.value)}
                                                id="price"
                                                name="price"
                                                label="Event Price"
                                                value={eventTicketPrice}
                                                fullWidth
                                                autoComplete="Event Price"
                                                type="number" sx={{ m: 1, minWidth: 150 }}
                                                variant="standard"
                                            />
                                        </Grid>
                                        <Grid item xs={12}>
                                            <FormHelperText id="description-helper-text">Description</FormHelperText>

                                            <TextareaAutosize onChange={event => setEventDescription(event.target.value)}
                                                maxRows={10}
                                                aria-label="maximum height"
                                                value={eventDescription}
                                                style={{ width: 500 }}
                                            />

                                        </Grid>
                                    </Grid>

                                </React.Fragment>
                            </Stack>
                        </DialogContent>
                        <DialogActions>
                            <Button onClick={() => { cancelChanges(); handleClose3() }} >Cancel </Button>
                            <Button onClick={() => {
                                submitChanges();
                            }} >Save</Button>
                        </DialogActions>
                    </Dialog>
                    <Dialog open={open4} onClose={handleClose4}  >
                        <DialogTitle>Participant List</DialogTitle>
                        <DialogContent>
                            <DialogContentText>
                            </DialogContentText>
                            <Stack direction="column" sx={{ width: '80%', margin: '30px' }} justifyContent="center" >

                                {rows.map((row) =>
                                (<div>
                                    {row.participantName}
                                </div>))}
                            </Stack>
                        </DialogContent>
                        <DialogActions>
                            <Button onClick={handleClose4} >Cancel </Button>
                        </DialogActions>
                    </Dialog>
                </Stack>
            </ListItem>
            <Divider variant="inset" component="li" />
        </>
    )
}