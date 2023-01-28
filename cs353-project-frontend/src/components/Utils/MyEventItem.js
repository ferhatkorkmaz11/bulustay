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
import "./Item.css"

export default function MyEventItem(props) {
    const [amount, setDiscountAmount] = React.useState('')
    const [open2, setOpen2] = React.useState(false)
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

    const cancelEnrollment = () => {
        let myHeaders = new Headers();
        myHeaders.append("Content-Type", "application/json");
        let requestOptions = {
            method: 'DELETE',
            redirect: 'follow'
          };
        
        fetch("http://localhost:8080/cancelEnrollment?eventId=" + props.eventID + "&userId=" + localStorage.getItem("userId"), requestOptions)
            .then(response => response.text())
            .then(result => {
                let resultJSON = JSON.parse(result);
                if(resultJSON.code === '200') {
                    window.location.reload(true);
                    alert(resultJSON.message)
                }
                else {
                    alert(resultJSON.message)
                }
              })
            .catch(error => console.log('error', error));
            handleClose10()
    }

    console.log(props)
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
                                    Organizer Name:
                                </Typography>
                                <br />
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Event Location: {props.city}, {props.district}, {props.neighbourhood} {props.building}, {props.streetNo}
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
                                    Price: {props.ticketPrice}
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
                    <Button
                        type="submit"
                        variant="contained"
                        onClick = {handleOpen10}
                        sx={{ mt: 3, mb: 2, background: "#92B4EC" }}
                    >
                        Cancel Enrollment
                    </Button>
                    <Dialog
                            open={open10}
                            onClose={handleClose10}
                            aria-labelledby="alert-dialog-title"
                            aria-describedby="alert-dialog-description"
                        >
                            <DialogTitle id="alert-dialog-title">
                                {"Cancel Enrollment?"}
                            </DialogTitle>
                            <DialogContent>
                                <DialogContentText id="alert-dialog-description">
                                    Are you sure you want to cancel this enrollment?
                                </DialogContentText>
                            </DialogContent>
                            <DialogActions>
                                <Button onClick={handleClose10}>No</Button>
                                <Button onClick={cancelEnrollment} autoFocus>
                                    Yes
                                </Button>
                            </DialogActions>
                        </Dialog>
                </Stack>
            </ListItem>
            <Divider variant="inset" component="li" />
        </>
    )
}