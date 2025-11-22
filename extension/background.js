// Default blocklist (can be extended dynamically)
const DEFAULT_BLOCKLIST = [
  "djsceisaca.tech",
  "phishing-domain.net",
  "evil-example.org"
];

// Initialize blocklist in storage if it doesn't exist
chrome.runtime.onInstalled.addListener(() => {
  chrome.storage.local.get(['blocklist'], (result) => {
    if (!result.blocklist) {
      chrome.storage.local.set({ blocklist: DEFAULT_BLOCKLIST });
    }
  });
});

chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
  if (changeInfo.url) {
    try {
      const url = new URL(changeInfo.url);
      
      // Get blocklist from storage
      chrome.storage.local.get(['blocklist'], (result) => {
        const blocklist = result.blocklist || DEFAULT_BLOCKLIST;
        
        for (const bannedSite of blocklist) {
          if (url.hostname.includes(bannedSite)) {
            const blockedUrl = chrome.runtime.getURL(`blocked.html#${encodeURIComponent(changeInfo.url)}`);
            chrome.tabs.update(tabId, {
              url: blockedUrl
            });
            break;
          }
        }
      });
    } catch (error) {
      console.log(`Could not parse URL: ${changeInfo.url}`);
    }
  }
});
