# MarketCap

MarketCap is a [Paper](https://papermc.io/) server plugin that scans all inventories in a world to estimate the number of certain items in circulation. This plugin was designed for use on a private server.

## Installation

```sh
git clone https://github.com/Ewpratten/MarketCap.git
./gradlew build
cp ./build/libs/marketcap.jar /path/to/plugins/directory
```

## Usage

```text
-- Public --
/mcap info - View market cap info for all tracked items

-- Admin --
/mcap addfilter [ITEM_NAME] - Add a new item to track
/mcap removefilter [ITEM_NAME] - Remove an item from being tracked
/mcap wipe - Wipe all databases
/mcap reload [chunk radius 0-3] - Force the plugin to re-search an area
```