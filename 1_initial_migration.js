const Migrations = artifacts.require("Migrations");
const Mayor = artifacts.require("Mayor");


module.exports = function(deployer, network, accounts) {
  deployer.deploy(Migrations);
  const candidates = [accounts[1], accounts[2], accounts[4], accounts[6]];
  const escrow = accounts[5];
  const quorum = 6;
  deployer.deploy(Mayor, candidates, escrow, quorum, {from: accounts[0]});
};
