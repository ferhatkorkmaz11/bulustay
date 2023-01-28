import React, { useEffect } from "react";
import Typography from '@mui/material/Typography';
import Navbar from "../AdminNavbar/Navbar";
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import DialogTitle from '@mui/material/DialogTitle';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import Box from '@mui/material/Box';
import { LocalizationProvider } from '@mui/x-date-pickers-pro';
import { AdapterDayjs } from '@mui/x-date-pickers-pro/AdapterDayjs';
import { DateRangePicker } from '@mui/x-date-pickers-pro/DateRangePicker';
import { styled } from '@mui/material/styles';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell, { tableCellClasses } from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Stack from '@mui/material/Stack';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import dayjs from 'dayjs';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Select from '@mui/material/Select';
import ContentDialog from './ContentDialog.js'

export default function AdminPanel() {
    function createUser(name, userId) {
        return { name, userId };
    }

    function createReport(adminId, title, reportId, content) {
        return { adminId, title, reportId, content };
    }

    const [users, setUsers] = React.useState([])
    const [banUsers, setBanUsers] = React.useState([])
    const [date, setDate] = React.useState('')
    const [reportType, setReportType] = React.useState(0)
    const [amount, setAmount] = React.useState(1)

    const handleAmount = (event) => {
        setAmount(event.target.value)
    }
    const handleReport = (event) => {
        setReportType(event.target.value)
    }

    const handleReportRequest = () => {
        let newDate = new Date(date)
        var myHeaders = new Headers();
        myHeaders.append("Content-Type", "application/json");

        var raw = JSON.stringify({
            "adminId": parseInt(localStorage.getItem("userId")),
            "month": parseInt(newDate.getMonth() + 1),
            "year": parseInt(newDate.getFullYear()),
            "reportType": parseInt(reportType),
            "resultAmount": parseInt(amount)
        });

        var requestOptions = {
            method: 'POST',
            headers: myHeaders,
            body: raw,
            redirect: 'follow'
        };

        fetch("http://localhost:8080/createReport", requestOptions)
            .then(response => response.text())
            .then(result => {
                let responseJSON = JSON.parse(result);
                if (responseJSON.code === '200') {
                    window.location.reload(true)
                    handleReport2Close()
                    alert(responseJSON.message);
                } else {
                    alert(responseJSON.message);
                }
            })
            .catch(error => console.log('error', error));
    }
    //FOR VISUALS, TAKEN FROM MATERIAL UI
    const StyledTableCell = styled(TableCell)(({ theme }) => ({
        [`&.${tableCellClasses.head}`]: {
            backgroundColor: theme.palette.common.black,
            color: theme.palette.common.white,
        },
        [`&.${tableCellClasses.body}`]: {
            fontSize: 14,
        },
    }));

    const StyledTableRow = styled(TableRow)(({ theme }) => ({
        '&:nth-of-type(odd)': {
            backgroundColor: theme.palette.action.hover,
        },
        // hide last border
        '&:last-child td, &:last-child th': {
            border: 0,
        },
    }));
    const [isLoading, setLoading] = React.useState(true);

    useEffect(() => {
        if (isLoading) {
            var requestOptions = {
                method: 'GET',
                redirect: 'follow'
            };

            var requestOptions2 = {
                method: 'GET',
                redirect: 'follow'
            };

            var requestOptions3 = {
                method: 'GET',
                redirect: 'follow'
            };

            fetch("http://localhost:8080/getAllReports", requestOptions3)
                .then(response => response.text())
                .then(result => {
                    let resultJSON = JSON.parse(result);
                    if (resultJSON.code === '200') {
                        let curBody = resultJSON.body;
                        let newRow = []
                        for (let curReport of curBody) {
                            newRow.push(createReport(curReport.adminId, curReport.title, curReport.reportId, curReport.content));
                        }
                        setReports(newRow);
                        setLoading(false);
                    } else {
                        alert(resultJSON.message)
                    }
                })
                .catch(error => console.log('error', error));

            fetch("http://localhost:8080/getBannedUsers", requestOptions2)
                .then(response => response.text())
                .then(result => {
                    let resultJSON = JSON.parse(result);
                    if (resultJSON.code === '200') {
                        let curBody = resultJSON.body;
                        let newRow = []
                        for (let curUser of curBody) {
                            newRow.push(createUser(curUser.name, curUser.userId));
                        }
                        setBanUsers(newRow);
                        setLoading(false);
                    } else {
                        alert(resultJSON.message)
                    }
                })
                .catch(error => console.log('error', error));

            fetch("http://localhost:8080/getAllUsers", requestOptions)
                .then(response => response.text())
                .then(result => {
                    let resultJSON = JSON.parse(result);
                    if (resultJSON.code === '200') {
                        let curBody = resultJSON.body;
                        let newRow = []
                        for (let curUser of curBody) {
                            newRow.push(createUser(curUser.name, curUser.userId));
                        }
                        setUsers(newRow);
                        setLoading(false);
                    } else {
                        alert(resultJSON.message)
                    }
                })
                .catch(error => console.log('error', error));
        }

    }, [isLoading]);
    //END OF VISUALS, TAKEN FROM MATERIAL UI
    const [email, setEmail] = React.useState('');
    const [pass, setPass] = React.useState('');
    const [name, setName] = React.useState('');
    const [reports, setReports] = React.useState([])
    const [report, setReport] = React.useState(false);

    const handleReportOpen = (position) => {
        setReport(true);
    };

    const handleReportClose = () => {
        setReport(false);
    };

    const [report2, setReport2] = React.useState(false);
    const handleReport2Open = (position) => {
        setReport2(true);
    };

    const handleReport2Close = () => {
        setReport2(false);
    };

    const handleBan = (userId) => {
        var myHeaders = new Headers();
        myHeaders.append("Content-Type", "application/json");

        var raw = JSON.stringify({
            "userId": parseInt(userId),
            "adminId": parseInt(localStorage.getItem("userId"))
        });

        var requestOptions = {
            method: 'POST',
            headers: myHeaders,
            body: raw,
            redirect: 'follow'
        };

        fetch("http://localhost:8080/banUser", requestOptions)
            .then(response => response.text())
            .then(result => {
                let resultJSON = JSON.parse(result);
                if (resultJSON.code === '200') {
                    window.location.reload(true);
                    alert(resultJSON.message)
                } else {
                    alert(resultJSON.message)
                }
            })
            .catch(error => console.log('error', error));
    }

    const handleUnban = (userId) => {
        var myHeaders = new Headers();
        myHeaders.append("Content-Type", "application/json");

        var raw = JSON.stringify({
            "userId": parseInt(userId),
            "adminId": parseInt(localStorage.getItem("userId"))
        });

        var requestOptions = {
            method: 'POST',
            headers: myHeaders,
            body: raw,
            redirect: 'follow'
        };

        fetch("http://localhost:8080/forgiveUser", requestOptions)
            .then(response => response.text())
            .then(result => {
                let resultJSON = JSON.parse(result);
                if (resultJSON.code === '200') {
                    window.location.reload();
                    alert(resultJSON.message)
                } else {
                    alert(resultJSON.message)
                }
            })
            .catch(error => console.log('error', error));
    }

    const [createAdmin, setCreateAdmin] = React.useState(false);
    const handleCreateAdminOpen = () => {
        setCreateAdmin(true);
    }
    const handleCreateAdminClose = () => {
        setCreateAdmin(false);
    };

    function ContentItem(row) {
        const [openContent, setOpenContent] = React.useState(false)
    
        const handleOpenContent = () => {
            setOpenContent(true)
        }
    
        const handleCloseContent = () => {
            setOpenContent(false)
        }
    
        return(
        <StyledTableRow key={row.reportId}>
            <StyledTableCell>{row.adminId}</StyledTableCell>
            <StyledTableCell component="th" scope="row">
                {row.title}
            </StyledTableCell>
            <StyledTableCell>{row.reportId}</StyledTableCell>
            <Button variant="outlined" align="right" onClick={handleOpenContent}>Content</Button>
            <Dialog open={openContent} onClose={handleCloseContent}  >
                <DialogContent>
                    <div>{row.content}</div>
                </DialogContent>
                <DialogActions>
                    <Button variant="outlined" onClick={handleCloseContent} > Close </Button>
                </DialogActions>
            </Dialog>
        </StyledTableRow>
        )
    }

    const [dateInterval, setDateInterval] = React.useState([null, null]);

    return (
        <>
            <Navbar />
            <Button variant="contained" onClick={handleCreateAdminOpen} color="success">
                Create Admin
            </Button>
            <Dialog open={createAdmin} onClose={handleCreateAdminClose}  >
                <DialogTitle> Create Admin </DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Please specify your choices.
                    </DialogContentText>
                    <TextField id="outlined-basic" label="Email" variant="outlined" value={email} onChange={text => setEmail(text.target.value)} sx={{ m: 1, minWidth: 150 }} />
                    <TextField id="outlined-basic" label="Password" variant="outlined" value={pass} onChange={text => setPass(text.target.value)} sx={{ m: 1, minWidth: 150 }} />
                    <TextField id="outlined-basic" label="Name" variant="outlined" value={name} onChange={text => setName(text.target.value)} sx={{ m: 1, minWidth: 150 }} />
                </DialogContent>
                <DialogActions>
                    <Button variant="outlined" onClick={() => {
                        setCreateAdmin(false)
                    }} >Cancel </Button>
                    <Button variant="outlined" type="submit" onClick={() => {
                        let myHeaders = new Headers();
                        myHeaders.append("Content-Type", "application/json");

                        let raw = JSON.stringify({
                            "email": email,
                            "name": name,
                            "password": pass
                        });

                        var requestOptions = {
                            method: 'POST',
                            headers: myHeaders,
                            body: raw,
                            redirect: 'follow'
                        };

                        fetch("http://localhost:8080/createAdmin", requestOptions)
                            .then(response => response.text())
                            .then(result => {
                                if (JSON.parse(result).code === '200') {
                                    setCreateAdmin(false)
                                    window.location.reload(false)
                                } else {
                                    alert(result.message)
                                }

                            })
                            .catch(error => alert("Something went wrong amirim."));

                    }} >Submit</Button>
                </DialogActions>
            </Dialog>

            <div style={{ margin: '10px' }}>
                <Stack direction="row">
                    <Button variant="outlined" onClick={handleReport2Open}>
                        Create Report
                    </Button>
                </Stack>
                <Dialog open={report2} onClose={handleReport2Close}  >
                    <DialogTitle> Create Report </DialogTitle>
                    <DialogContent>
                        <FormControl sx={{ m: 1, minWidth: 150 }}>
                            <InputLabel id="dropdown-report-type">Report Type</InputLabel>
                            <Select
                                labelId="dropdown-report-type"
                                id="dropdown-report"
                                value={reportType}
                                onChange={handleReport}
                                label="Report Type"
                            >
                                <MenuItem value={0}>Most Participated Event</MenuItem>
                                <MenuItem value={1}>Total Enrollments to Event Types</MenuItem>
                                <MenuItem value={2}>Total Enrollments to Organizers' Events</MenuItem>
                            </Select>
                        </FormControl>
                        {(reportType !== 1) ? <TextField id="outlined-basic" value={amount} onChange={handleAmount} label="Event Amount" variant="outlined" type="number" min="0" oninput="validity.valid||(value='');" sx={{ m: 1, minWidth: 150 }} /> : <div></div>}
                        <br />
                        <LocalizationProvider
                            dateAdapter={AdapterDayjs}
                            localeText={{ start: 'Start Date', end: 'End Date' }}
                        >
                            <DatePicker
                                views={['year', 'month']}
                                label="Year and Month"
                                value={date}
                                onChange={(newDate) => {
                                    setDate(newDate);
                                }}
                                renderInput={(params) => <TextField {...params} helperText={null} />}
                            />
                        </LocalizationProvider>
                    </DialogContent>
                    <DialogActions>
                        <Button variant="outlined" onClick={() => {
                            setReport2(false)
                        }} >Cancel </Button>
                        <Button variant="outlined" onClick={handleReportRequest}>Submit</Button>
                    </DialogActions>
                </Dialog>

            </div>
            <Typography style={{ margin: '10px' }}>Reports</Typography>
            <TableContainer component={Paper} sx={{ width: '50%' }}>
                <Table size='small' aria-label="customized table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell>Admin ID</StyledTableCell>
                            <StyledTableCell>Title</StyledTableCell>
                            <StyledTableCell>Report ID</StyledTableCell>
                            <StyledTableCell></StyledTableCell>
                            <StyledTableCell></StyledTableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {reports.map((report) => (
                            <ContentDialog {...report}/>   
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>


            <Typography style={{ margin: '10px' }}>User List</Typography>
            <TableContainer component={Paper} sx={{ width: '50%' }}>
                <Table size='small' aria-label="customized table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell>User ID</StyledTableCell>
                            <StyledTableCell>Name</StyledTableCell>
                            <StyledTableCell></StyledTableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {users.map((row) => (
                            <StyledTableRow key={row.userId}>
                                <StyledTableCell>{row.userId}</StyledTableCell>
                                <StyledTableCell component="th" scope="row">
                                    {row.name}
                                </StyledTableCell>
                                <Button variant="outlined" align="right" onClick={handleBan.bind(this, row.userId)}>Ban</Button>
                            </StyledTableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            <Typography style={{ margin: '10px' }}>Banned User List</Typography>
            <TableContainer component={Paper} sx={{ width: '50%' }}>
                <Table size='small' aria-label="customized table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell>User Id</StyledTableCell>
                            <StyledTableCell>Name</StyledTableCell>
                            <StyledTableCell></StyledTableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {banUsers.map((row) => (
                            <StyledTableRow key={row.userId}>
                                <StyledTableCell>{row.userId}</StyledTableCell>
                                <StyledTableCell component="th" scope="row">
                                    {row.name}
                                </StyledTableCell>
                                <Button variant="outlined" align="right" onClick={handleUnban.bind(this, row.userId)}>Forgive</Button>
                            </StyledTableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </>
    )
}