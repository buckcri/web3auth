"use strict";

const e = React.createElement;

/**
 * This component is the client side for the Web3 client-server authentication flow. It consists of several state transitions. All communication with the backend is done asynchronously.
 *
 * At first, the user must allow connecting to their Web3 wallet.
 * Then, the user may start the auth flow by clicking the respective button. A request for a challenge is sent to the backend.
 * Upon reception of the challenge, the user is prompted to sign the challenge with their wallet.
 * After signing, the signature is sent to backend. If signature validation is successful, a JWT is received.
 * Finally, the payload of the JWT is displayed and the auth flow finishes.
 */
class Web3Auth extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      // true if connected to Web3 wallet
      isConnected: false,
      // true if connected, requested challenge, and received challenge token from backend
      hasChallenge: false,
      // challenge data received from backend to sign with wallet
      challenge: null,
      // account to authenticate with
      account: null,
      // id_token received on successful authentication
      token: null,
    };
  }

  // Connecting to wallet is the first state in this component
  async componentDidMount() {
    if (window.ethereum) {
      let accounts = await window.ethereum.request({
        method: "eth_requestAccounts",
      });
      window.web3 = new Web3(window.ethereum);
      this.setState({
        isConnected: true,
        account: accounts[0],
      });
    }
  }

  async componentDidUpdate() {
    // User initiated auth flow by clicking "Begin Authentication" button, and a challenge was received from the backend. Now to sign the sha3 of the nonce in the challenge and send it back to the backend.
    if (this.state.hasChallenge && null == this.state.token) {
      /* see https://github.com/MetaMask/providers/issues/199 for reason behind signing the hashed nonce instead of nonce itself */
      window.ethereum
        .request({
          method: "personal_sign",
          params: [
            window.web3.utils.toHex(window.web3.utils.sha3(this.state.challenge.nonce)),
            this.state.account,
          ],
        })
        .then((signature) => {
          const url = new URL("http://localhost:8080/response");
          fetch(url, {
            method: "POST",
            headers: {
              Accept: "application/json",
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              signedMessage: signature,
              challengedAccount: this.state.account,
            }),
          }).then((res) => {
            if (!res.ok) {
              res.json().then((err) => {
                alert(JSON.stringify(err));
                // Reset state to request a new challenge
                this.setState({
                  hasChallenge: false,
                });
              });
            } else {
              return res.text().then((text) => {
                this.setState({
                  token: text,
                });
              });
            }
          });
        });
    }
  }

  disabledSpinButton(header, label) {
    return (
      <div className="mt-2">
        {header}
        <br />
        <button className="btn btn-primary" type="button" disabled>
          <span
            className="spinner-border spinner-border-sm"
            role="status"
            aria-hidden="true"
          ></span>
          <span className="sr-only">{label}</span>
        </button>
      </div>
    );
  }

  render() {
    // This state is reached if no wallet to connect is available, i.e. the auth flow cannot be started.
    if (!window.ethereum) {
      return <div className="mt-2">No wallet found.</div>;
    }

    // This is the end state after a successful challenge-response flow. Simply display the payload of the received JWT.
    if (this.state.token != null) {
      return (
        <div>
          <div className="alert alert-success" role="alert">
            Successfully authenticated
          </div>
          <div className="mt-2">
            {JSON.stringify(JSON.parse(atob(this.state.token.split(".")[1])))}
          </div>
        </div>
      );
    }

    // This is the waiting state while trying to connect the wallet.
    if (!this.state.isConnected) {
      return this.disabledSpinButton(
        "Please unlock your wallet and allow connecting.",
        "Waiting for connection"
      );
    }

    // This state is entered after receiving the challenge from the backend and we are waiting for the user to sign the nonce with their wallet. See #componentDidUpdate for the signing logic.
    if (this.state.hasChallenge) {
      return this.disabledSpinButton(
        "Please sign the authentication challenge message with your wallet.",
        "Waiting for signature"
      );
    }

    // This state is entered when there is a successful connection to the wallet and the user may now start the auth flow.
    if (!this.state.hasChallenge) {
      return (
        <button
          className="btn btn-primary"
          onClick={() => {
            const url = new URL("http://localhost:8080/request");

            fetch(url, {
              method: "POST",
              headers: {
                Accept: "application/json",
                "Content-Type": "application/json",
              },
              body: JSON.stringify({
                account: this.state.account,
              }),
            }).then((res) => {
              if (!res.ok) {
                res.json().then((err) => {
                  alert(JSON.stringify(err));
                });
              } else {
                return res.json().then((json) => {
                  this.setState({
                    hasChallenge: true,
                    challenge: json,
                  });
                });
              }
            });
          }}
        >
          Begin Authentication
        </button>
      );
    }
  }
}

const domContainer = document.querySelector("#login_container");
ReactDOM.render(e(Web3Auth), domContainer);
