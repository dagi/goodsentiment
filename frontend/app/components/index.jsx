import { Component } from 'react';
import cx from 'classnames';
import omit from 'lodash/object/omit';
import isUndefined from 'lodash/lang/isUndefined';
import isNull from 'lodash/lang/isNull';
import $ from 'jquery';

const qr = require('../assets/qr.svg');

const DEPARTMENTS = [
    'DevOps',
    'PM',
    'TPM/UX',
    'Support',
    'Services',
    'Sales',
    'Marketing',
    'Finance',
    'HR',
    'Other'
];

const YEARS = [
    ['<1', 0],
    ['1', 1],
    ['2', 2],
    ['3', 3],
    ['4', 4],
    ['5', 5],
    ['6', 6],
    ['7', 7],
    ['8+', 8]
];

const SECONDS_TO_PROVIDE_MORE_INFO = 10;
const SECONDS_TO_DISPLAY_THANK_YOU = 3;

const setFailedItems = failedItems => localStorage.setItem('failed', JSON.stringify(failedItems));
const getFailedItems = () => JSON.parse(localStorage.getItem('failed')) || {};
const isDefined = val => !isUndefined(val) && !isNull(val);

export default class Index extends Component {
    constructor(props) {
        super(props);

        this.state = this.getInitialState();

        this.saveFailedAnswersTimeout = setInterval(this.saveFailedAnswers.bind(this), 1 * 60 * 1000);
    }

    getInitialState() {
        return {
            page: 'sentiment',
            feeling: null,
            department: null,
            year: null
        };
    }

    renderPage() {
        if (this.state.page === 'sentiment') {
            return this.renderSentiment();
        } else if (this.state.page === 'department') {
            return this.renderDepartment();
        } else {
            return this.renderThankYou();
        }
    }

    renderSentiment() {
        return (
            <div>
                <header className="header"></header>
                <main className="main page-1">
                    <h1>How does GoodData <br />make you feel today?</h1>
                    <div className="sentiments">
                        <a href="#" className="sentiment sentiment-happy" onClick={this.sendSentiment.bind(this, 100)}></a>
                        <a href="#" className="sentiment sentiment-neutral" onClick={this.sendSentiment.bind(this, 0)}></a>
                        <a href="#" className="sentiment sentiment-sad" onClick={this.sendSentiment.bind(this, -100)}></a>
                    </div>

                    <img className="qr" src={qr} />
                </main>
            </div>
        );
    }

    renderDepartment() {
        return (
            <div>

                <header className="header">Redirecting back to homepage in {SECONDS_TO_PROVIDE_MORE_INFO} s...</header>

                <main className="main page-2">
                    <h1>Thanks!</h1>
                    <h3>Optionally you can tell us more...</h3>

                    <div className="button-group expanded">
                        <h2>Where do you belong?</h2>
                        <div className="buttons">
                            {DEPARTMENTS.map(department => {
                                return (
                                    <label className="button button-large" onClick={e => { this.setAdditionalInfo({ department }); }}><input type="radio" name="where"/><span className="button-label">{department}</span></label>
                                );
                            })}
                        </div>
                    </div>

                    <div className="button-group invisible">
                        <h2>How many years have you been here?</h2>
                        <div className="buttons">
                            {YEARS.map(year => {
                                const text = year[0];
                                const value = year[1];

                                return (
                                    <label className="button button-small" onClick={e => { this.setAdditionalInfo({ year: value }); }}><input type="radio" name="where"/><span className="button-label">{text}</span></label>
                                );
                            })}
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    renderThankYou() {
        return (
            <div>
                <header className="header">Redirecting back to homepage in {SECONDS_TO_DISPLAY_THANK_YOU} s...</header>

                <main className="main page-3">
                    <h1>Thank you!</h1>
                    <h3>Your feedback matters (really).</h3>
                </main>
            </div>
        );
    }

    sendSentiment(feeling) {
        this.saveAfter(SECONDS_TO_PROVIDE_MORE_INFO);

        this.setState({
            page: 'department',
            feeling
        });
    }

    saveFailedAnswers() {
        const failedItems = getFailedItems();
        Object.keys(failedItems).map(key => failedItems[key]).map(info => this.saveInfo(info));
    }

    saveAfter(seconds) {
        this.removeSaveTimer();

        this.saveTimeout = setTimeout(() => {
            const info = omit({
                sentimentCode: this.state.feeling,
                orgUnit: this.state.department,
                yearsInCompany: this.state.year,
                timestamp: Date.now()
            }, item => isUndefined(item) || isNull(item));

            this.saveInfo(info);

            this.setState({
                page: 'thankyou',
            }, () => {
                setTimeout(this.resetApp.bind(this), SECONDS_TO_DISPLAY_THANK_YOU * 1000);
            });
        }, seconds * 1000);
    }

    saveInfo(info) {
        $.ajax({
            url: 'https://pgg0bstvr3.execute-api.eu-west-1.amazonaws.com/prod/vote',
            type: 'POST',
            data: JSON.stringify(info)
        }).then(this.savingSuccessFull.bind(this, info), this.savingFailed.bind(this, info));
    }

    savingSuccessFull(info) {
        const failedItems = getFailedItems();

        if (failedItems[info.time]) {
            delete failedItems[info.time];

            setFailedItems(failedItems);
        }
    }

    savingFailed(info) {
        const failedItems = getFailedItems();

        failedItems[info.time] = info;

        setFailedItems(failedItems);
    }

    removeSaveTimer() {
        if (this.saveTimeout) {
            clearTimeout(this.saveTimeout);
            this.saveTimeout = null;
        }
    }

    resetApp() {
        this.removeSaveTimer();
        this.setState(this.getInitialState());
    }

    setAdditionalInfo(info) {
        if ((isDefined(info.year) && isDefined(this.state.department)) || (isDefined(info.department) && isDefined(this.state.year))) {
            this.saveAfter(0);
        } else {
            this.saveAfter(SECONDS_TO_PROVIDE_MORE_INFO);
        }

        this.setState(info);
    }

    render() {
        return (
            <div>
                {this.renderPage()}
            </div>
        );
    }
};
