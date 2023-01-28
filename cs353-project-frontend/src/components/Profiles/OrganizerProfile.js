import React, { useEffect } from 'react';
import ProfileUi from 'react-profile-card';
import { Stack } from '@mui/system';
import ListItem from '@mui/material/ListItem';
import Divider from '@mui/material/Divider';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Checkbox from '@mui/material/Checkbox';
import FavoriteBorder from '@mui/icons-material/FavoriteBorder';
import Favorite from '@mui/icons-material/Favorite';
import { useHistory } from 'react-router-dom';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';
import { FormGroup } from '@mui/material';
import Navbar from '../Navbar/Navbar';


function OrganizerProfile() {
    const history = useHistory()
    const [fName, setfName] = React.useState('');
    const [lName, setlName] = React.useState('');
    const [name, setName] = React.useState()
    const [isLoading, setLoading] = React.useState(true)
    const [email, setEmail] = React.useState("");
    const [open, setOpen] = React.useState(false);
    const [donationAmount, setDonationAmount] = React.useState(0);
    const [balance, setBalance] = React.useState(0);
    const [followerNumber, setFollowerNumber] = React.useState(0);
    const [eventNumber, setEventNumber] = React.useState(0);
    const [isFollowing, setIsFollowing] = React.useState(false);


    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    fetch("http://localhost:8080/getFollowers?organizerId=" + history.location.state?.organizerID , requestOptions)
        .then(response => response.text())
        .then(result => {
            let resultJSON = JSON.parse(result);
            if (resultJSON.code === '200') {
                setFollowerNumber(resultJSON.body.length);
                let curBody = resultJSON.body;
                for (let row of curBody) {
                    if (row.participantId === parseInt(localStorage.getItem("userId"))) {
                        setIsFollowing(true);
                    }
                }
            }
            else {
                alert(resultJSON.message)
            }
        })
        .catch(error => console.log('error', error));


    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    fetch("http://localhost:8080/getOrganizerEvents?organizerId=" + history.location.state?.organizerID + "&userId=" + localStorage.getItem("userId"), requestOptions)
        .then(response => response.text())
        .then(result => {
            let resultJSON = JSON.parse(result);
            if (resultJSON.code === '200') {
                setEventNumber(resultJSON.body.length);
            }
            else {
                alert(resultJSON.message)
            }
        })
        .catch(error => console.log('error', error));

    const handleClickOpen = (position) => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };


    const unfollow = () => {
        var requestOptions = {
            method: 'DELETE',
            redirect: 'follow'
        };

        fetch("http://localhost:8080/unfollow?participantId=" + localStorage.getItem("userId") + "&organizerId=" + history.location.state?.organizerID, requestOptions)
            .then(response => response.text())
            .then(result => {
                let responseJSON = JSON.parse(result);
                if(responseJSON.code === '200')
                {
                    window.location.reload(true)
                }
                else {
                    alert("Something went wrong.")
                }
            })
            .catch(error => console.log('error', error));
        


    }


    const addFollower = () => {
        var myHeaders = new Headers();
        myHeaders.append("Content-Type", "application/json");

        var raw = JSON.stringify({
            "participantId": parseInt(localStorage.getItem("userId")),
            "organizerId": parseInt(history.location.state?.organizerID)
        });

        var requestOptions = {
            method: 'POST',
            headers: myHeaders,
            body: raw,
            redirect: 'follow'
        };

        fetch("http://localhost:8080/follow", requestOptions)
            .then(response => response.text())
            .then(result => {
                let responseJSON = JSON.parse(result);
                if(responseJSON.code === '200')
                {
                    window.location.reload(true)
                }
                else {
                    alert("Something went wrong.")
                    
            }})
            .catch(error => console.log('error', error));


    }

    const makeDonation = () => {

        var myHeaders = new Headers();
        myHeaders.append("Content-Type", "application/json");

        var raw = JSON.stringify({
            "amount": donationAmount,
            "participantId": parseInt(localStorage.getItem("userId")),
            "organizerId": parseInt(history.location.state?.organizerID)
        });

        var requestOptions = {
            method: 'POST',
            headers: myHeaders,
            body: raw,
            redirect: 'follow'
        };

        fetch("http://localhost:8080/createDonation", requestOptions)
            .then(response => response.text())
            .then(result => {
                window.location.reload(true)
            })
            .catch(error => console.log('error', error));
        

    }




    useEffect(() => {
        let requestOptions = {
            method: 'GET',
            redirect: 'follow'
        };
        fetch("http://localhost:8080/myProfile?userId=" + history.location.state?.organizerID, requestOptions)
            .then(response => response.text())
            .then(result => {
                let resultJSON = JSON.parse(result);
                if (resultJSON.code === '200') {
                    let resultBody = resultJSON.body;
                    setfName(resultBody.name.split(" ")[0]);
                    setlName(resultBody.name.split(" ")[1]);
                    //setBalance(resultBody.balance);
                    setName(resultBody.name);
                    setEmail(resultBody.email)
                }
                else {
                    alert(resultJSON.message)
                }
            })
            .catch(error => console.log('error', error));

            fetch("http://localhost:8080/myProfile?userId=" + localStorage.getItem("userId"), requestOptions)
            .then(response => response.text())
            .then(result => {
                let resultJSON = JSON.parse(result);
                if (resultJSON.code === '200') {
                    let resultBody = resultJSON.body;
                    setBalance(resultBody.balance);
                }
                else {
                    alert(resultJSON.message)
                }
            })
            .catch(error => console.log('error', error));

    }, [isLoading]);

    return (
        <>
        <Navbar></Navbar>
        <Stack direction="column" sx={{ width: '100%', margin: '0px' }} alignItems="center" >
            <Dialog open={open} onClose={handleClose}  >
                <DialogContent>
                    <DialogContentText>
                        Current Balance: {balance}
                    </DialogContentText>

                    <React.Fragment>
                        <Typography variant="h6" gutterBottom>
                            Payment method
                        </Typography>
                        <FormGroup row sx={{ paddingBottom: 2 }} >
                            <Grid container spacing={3}>
                                <Grid item xs={12} md={6}>
                                    <TextField onChange={event => setDonationAmount(event.target.value)}
                                        required
                                        id="donationAmount"
                                        label="Amount"
                                        type="number"
                                        fullWidth
                                        autoComplete="amount"
                                        variant="standard"
                                    />
                                </Grid>
                                <Grid item xs={12}>

                                </Grid>
                            </Grid>
                        </FormGroup>
                        <Button
                            variant="contained" type='submit'
                            onClick={() => {
                                makeDonation(); setOpen(false);
                            }}
                            sx={{ mt: 4, background: "#92B4EC" }}
                        >
                            Submit
                        </Button>    </React.Fragment>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => {
                        setOpen(false)
                    }} >Close </Button>

                </DialogActions>
            </Dialog>
            <ProfileUi
                imgUrl='https://ualr.edu/diversity/files/2018/02/Untitled-design.png'
                name={name}
            />
            <ListItem sx={{ border: 2, borderColor: '#fe7c70', bgcolor: '#edecf0', width: '800px' }}>
                <Stack direction="row" alignItems="center" justifyContent={'center'} sx={{ width: '100%' }} spacing={2}>
                    <ListItemText
                        secondary={
                            <React.Fragment >

                                <Typography
                                    component="span"
                                    variant="body1"
                                >
                                    Followers: {followerNumber}
                                </Typography>
                                <br /> <br />
                                <Typography
                                    component="span"
                                    variant="body1"
                                >
                                    Number of Events: {eventNumber}
                                </Typography>
                                <br /> <br />
                                <Typography
                                    component="span"
                                    variant="body1"
                                >
                                    Contact: {email}
                                </Typography>

                            </React.Fragment>
                        }
                    />
                    {isFollowing ?

                        <Button
                            //href = "/home"
                            type="submit"
                            variant="contained"
                            onClick={unfollow}
                            sx={{ mt: 3, mb: 2, background: "#92B4EC" }}
                        >
                            - Unfollow
                        </Button>
                        :
                        <Button
                            //href = "/home"
                            type="submit"
                            variant="contained"
                            onClick={addFollower}
                            sx={{ mt: 3, mb: 2, background: "#92B4EC" }}
                        >
                            + Follow
                        </Button>

                    }
                    <Button
                        //href = "/home"
                        type="submit"
                        variant="contained"
                        onClick={handleClickOpen}
                        sx={{ mt: 3, mb: 2, background: "#92B4EC" }}
                    >
                        $ Donate
                    </Button>

                </Stack>
            </ListItem>
        </Stack>
        </>
    );
}

export default OrganizerProfile;