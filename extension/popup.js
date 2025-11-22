// SVG Icon helper function
function getIconSVG(iconName) {
  const icons = {
    'lock': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="5" y="11" width="14" height="10" rx="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'warning': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 9v4m0 4h.01M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'folder': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M3 7v10a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-6l-2-2H5a2 2 0 0 0-2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'target': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="12" r="6" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="12" r="2" stroke="currentColor" stroke-width="2"/></svg>',
    'text': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M4 7h4m0 0V4m0 3v3m6-3h4m0 0V4m0 3v3M4 17h16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'search': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/><path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'eye': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/></svg>',
    'ruler': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M3.27 6.96 12 12.01l8.73-5.05M12 22.08V12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'building': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M3 21h18M5 21V7l8-4v18M19 21V11l-6-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'minus': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'shield': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 2L4 5v6c0 5.55 3.84 10.74 8 12 4.16-1.26 8-6.45 8-12V5l-8-3z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'mask': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M9 11a3 3 0 1 0 6 0 3 3 0 0 0-6 0z" stroke="currentColor" stroke-width="2"/><path d="M17.5 11a2.5 2.5 0 1 0 5 0 2.5 2.5 0 0 0-5 0z" stroke="currentColor" stroke-width="2"/><path d="M1.5 11a2.5 2.5 0 1 0 5 0 2.5 2.5 0 0 0-5 0z" stroke="currentColor" stroke-width="2"/></svg>',
    'link': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'plug': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 22v-5M7 7H5a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2M17 7h2a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2h-2M8 7V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v3M8 17v3a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2v-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'globe': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" stroke="currentColor" stroke-width="2"/></svg>',
    'x-circle': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="m15 9-6 6m0-6 6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'dice': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/><circle cx="9" cy="9" r="1.5" fill="currentColor"/><circle cx="15" cy="9" r="1.5" fill="currentColor"/><circle cx="9" cy="15" r="1.5" fill="currentColor"/><circle cx="15" cy="15" r="1.5" fill="currentColor"/><circle cx="12" cy="12" r="1.5" fill="currentColor"/></svg>',
    'clock': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>',
    'folder-open': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 19a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v1M5 19h14a2 2 0 0 0 2-2v-5a2 2 0 0 0-2-2H9a2 2 0 0 0-2 2v5a2 2 0 0 1-2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'key': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0 3 3L22 7l-3-3m-3.5 3.5L19 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    'lock-closed': '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="2"/></svg>',
  };
  return icons[iconName] || icons['globe'];
}

function analyzeUrlLocally(urlString) {
  let analysis = [];
  const sensitiveKeywords = [
    'login', 'verify', 'account', 'password', 'secure', 'signin', 'banking',
    'confirm', 'webscr', 'ebayisapi', 'paypal', 'update', 'validate', 'activate',
    'suspended', 'locked', 'urgent', 'action-required', 'verify-account'
  ];

  try {
    const url = new URL(urlString);
    const hostname = url.hostname;
    const path = url.pathname;
    const query = url.search;
    const protocol = url.protocol;
    const port = url.port;
    const fullUrl = urlString.toLowerCase();

    // Protocol & Security Analysis
    if (protocol === "https:") {
      analysis.push({ 
        text: "Secure HTTPS protocol", 
        type: "good",
        icon: "lock",
        severity: 0,
        category: "Security"
      });
    } else if (protocol === "http:") {
      analysis.push({ 
        text: "Unencrypted HTTP (data transmission vulnerable)", 
        type: "bad",
        icon: "warning",
        severity: 8,
        category: "Security"
      });
    }

    // IP Address Detection
    const ipv4Regex = /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/;
    const ipv6Regex = /^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$/;
    if (ipv4Regex.test(hostname) || ipv6Regex.test(hostname)) {
      analysis.push({ 
        text: "Direct IP address (bypasses DNS, highly suspicious)", 
        type: "bad",
        icon: "target",
        severity: 9,
        category: "Phishing"
      });
    }

    // Homograph Attack Detection
    const homographChars = /[а-яА-Я\u0400-\u04FF\u0430-\u044f\u00e0-\u00ff]/;
    if (homographChars.test(hostname)) {
      analysis.push({ 
        text: "Potential homograph attack (Unicode characters resembling Latin)", 
        type: "bad",
        icon: "text",
        severity: 10,
        category: "Phishing"
      });
    }

    // Typosquatting Detection
    const commonTypos = ['amaz0n', 'go0gle', 'faceb00k', 'microsft', 'appple', 'yaho0', 'ebayy', 'paypall'];
    const hostLower = hostname.toLowerCase();
    for (const typo of commonTypos) {
      if (hostLower.includes(typo)) {
        analysis.push({ 
          text: `Possible typosquatting detected (${typo})`, 
          type: "bad",
          icon: "search",
          severity: 10,
          category: "Phishing"
        });
        break;
      }
    }

    // Suspicious Characters
    if (urlString.includes('@')) {
      analysis.push({ 
        text: "'@' symbol detected (can hide real domain in browser)", 
        type: "bad",
        icon: "eye",
        severity: 9,
        category: "Phishing"
      });
    }

    // URL Length & Complexity
    if (urlString.length > 200) {
      analysis.push({ 
        text: `URL excessively long (${urlString.length} chars) - obfuscation tactic`, 
        type: "bad",
        icon: "ruler",
        severity: 7,
        category: "Obfuscation"
      });
    } else if (urlString.length > 100) {
      analysis.push({ 
        text: `URL is very long (${urlString.length} chars)`, 
        type: "caution",
        icon: "ruler",
        severity: 4,
        category: "Obfuscation"
      });
    }

    // Subdomain Analysis
    const dotCount = (hostname.match(/\./g) || []).length;
    if (dotCount > 5) {
      analysis.push({ 
        text: `Excessive subdomains (${dotCount} levels) - suspicious nesting`, 
        type: "bad",
        icon: "building",
        severity: 8,
        category: "Phishing"
      });
    } else if (dotCount > 3) {
      analysis.push({ 
        text: `Multiple subdomains (${dotCount} levels)`, 
        type: "caution",
        icon: "building",
        severity: 5,
        category: "Phishing"
      });
    }

    // Hyphen Analysis
    const hyphenCount = (hostname.match(/-/g) || []).length;
    if (hyphenCount > 3) {
      analysis.push({ 
        text: `Multiple hyphens in domain (${hyphenCount}) - common in phishing`, 
        type: "caution",
        icon: "minus",
        severity: 6,
        category: "Phishing"
      });
    }

    // Deceptive Patterns
    if (hostname.includes('https-') || hostname.includes('http-') || 
        hostname.startsWith('https.') || hostname.startsWith('http.') ||
        hostname.includes('secure-') || hostname.includes('ssl-')) {
      analysis.push({ 
        text: "Deceptive pattern: security-related keywords in hostname", 
        type: "bad",
        icon: "shield",
        severity: 9,
        category: "Phishing"
      });
    }

    // Brand Mimicking
    const brandNames = ['google', 'microsoft', 'apple', 'amazon', 'facebook', 'paypal', 'ebay', 'netflix', 'twitter', 'instagram'];
    const mainDomain = hostname.split('.').slice(-2, -1)[0];
    for (const brand of brandNames) {
      if (mainDomain.includes(brand) && mainDomain !== brand) {
        analysis.push({ 
          text: `Possible brand impersonation (${mainDomain} vs ${brand})`, 
          type: "bad",
          icon: "mask",
          severity: 10,
          category: "Phishing"
        });
        break;
      }
    }

    // Shortened URL Detection
    const shortenerDomains = ['bit.ly', 'tinyurl.com', 't.co', 'goo.gl', 'ow.ly', 'is.gd', 'buff.ly'];
    if (shortenerDomains.some(short => hostname.includes(short))) {
      analysis.push({ 
        text: "URL shortening service detected (destination unknown)", 
        type: "caution",
        icon: "link",
        severity: 6,
        category: "Obfuscation"
      });
    }

    // Port Analysis
    if (port) {
      const portNum = parseInt(port);
      if (portNum !== 80 && portNum !== 443 && portNum !== 8080) {
        analysis.push({ 
          text: `Non-standard port (${port}) - may indicate evasion`, 
          type: "caution",
          icon: "plug",
          severity: 5,
          category: "Security"
        });
      }
    }

    // Path Analysis
    if (path.includes('//') || path.includes('../') || path.includes('..\\')) {
      analysis.push({ 
        text: "Path traversal or double-slash redirect detected", 
        type: "bad",
        icon: "folder-open",
        severity: 8,
        category: "Security"
      });
    }

    const suspiciousPaths = ['/login.php', '/wp-admin', '/admin', '/cgi-bin', '/phpmyadmin'];
    if (suspiciousPaths.some(sp => path.toLowerCase().includes(sp))) {
      analysis.push({ 
        text: `Suspicious path pattern detected: ${path}`, 
        type: "caution",
        icon: "folder-open",
        severity: 4,
        category: "Security"
      });
    }

    // Query Parameter Analysis
    if (query) {
      const suspiciousParams = ['token', 'key', 'password', 'pwd', 'secret', 'auth', 'email', 'user', 'login'];
      const urlParams = new URLSearchParams(query);
      const foundParams = [];
      
      urlParams.forEach((value, key) => {
        if (suspiciousParams.some(sp => key.toLowerCase().includes(sp))) {
          foundParams.push(key);
        }
      });

      if (foundParams.length > 0) {
        analysis.push({ 
          text: `Sensitive parameters in URL: ${foundParams.join(', ')}`, 
          type: "caution",
          icon: "key",
          severity: 6,
          category: "Security"
        });
      }

      if (query.includes('%') && (query.match(/%/g) || []).length > 5) {
        analysis.push({ 
          text: "High URL encoding detected (possible obfuscation)", 
          type: "caution",
          icon: "lock-closed",
          severity: 5,
          category: "Obfuscation"
        });
      }
    }

    // Keyword Analysis
    const foundKeywords = sensitiveKeywords.filter(kw => fullUrl.includes(kw));
    if (foundKeywords.length > 2) {
      analysis.push({ 
        text: `Multiple suspicious keywords detected: ${foundKeywords.slice(0, 3).join(', ')}`, 
        type: "bad",
        icon: "search",
        severity: 7,
        category: "Phishing"
      });
    } else if (foundKeywords.length > 0) {
      analysis.push({ 
        text: `Contains sensitive keywords: ${foundKeywords.join(', ')}`, 
        type: "caution",
        icon: "search",
        severity: 4,
        category: "Phishing"
      });
    }

    // Domain Name Analysis
    if (/\d/.test(hostname.split('.')[0]) && hostname.split('.')[0].length > 3) {
      const numInDomain = hostname.match(/\d/g);
      if (numInDomain && numInDomain.length > 1) {
        analysis.push({ 
          text: "Numbers in domain name (possible typosquatting)", 
          type: "caution",
          icon: "text",
          severity: 5,
          category: "Phishing"
        });
      }
    }

    // TLD Analysis
    const suspiciousTLDs = ['.tk', '.ml', '.ga', '.cf', '.gq', '.xyz', '.top', '.click'];
    const tld = hostname.split('.').pop();
    if (suspiciousTLDs.includes('.' + tld)) {
      analysis.push({ 
        text: `Suspicious TLD detected (.${tld}) - commonly used for malicious sites`, 
        type: "caution",
        icon: "globe",
        severity: 6,
        category: "Reputation"
      });
    }

    // Entropy Analysis
    const domainPart = hostname.split('.')[0];
    const entropy = calculateEntropy(domainPart);
    if (entropy > 4.5 && domainPart.length > 10) {
      analysis.push({ 
        text: "High domain entropy (random-looking, possibly DGA-generated)", 
        type: "caution",
        icon: "dice",
        severity: 6,
        category: "Malware"
      });
    }

  } catch (error) {
    analysis.push({ 
      text: "Cannot parse URL structure", 
      type: "bad",
      icon: "x-circle",
      severity: 5,
      category: "Error"
    });
  }

  return analysis;
}

function calculateEntropy(str) {
  const freq = {};
  for (let i = 0; i < str.length; i++) {
    const char = str[i];
    freq[char] = (freq[char] || 0) + 1;
  }
  let entropy = 0;
  for (const char in freq) {
    const p = freq[char] / str.length;
    entropy -= p * Math.log2(p);
  }
  return entropy;
}

async function fetchDnsInfo(hostname) {
  if (hostname.includes('.') === false || hostname === 'localhost' || hostname.startsWith('127.') || hostname.startsWith('192.168.')) {
    return { error: "Cannot query DNS for local or invalid host." };
  }
  try {
    const response = await fetch(`https://dns.google/resolve?name=${hostname}&type=A`);
    if (!response.ok) { 
      return { error: "DNS API request failed." }; 
    }
    const json = await response.json();
    return json;
  } catch (error) {
    return { error: "Failed to fetch DNS data." };
  }
}

function calculateRating(localResults, dnsData) {
  let score = 100;
  let threatCount = { bad: 0, caution: 0, good: 0 };
  let categories = {};

  localResults.forEach(result => {
    if (result.type === "bad") {
      score -= result.severity || 15;
      threatCount.bad++;
    } else if (result.type === "caution") {
      score -= result.severity || 5;
      threatCount.caution++;
    } else if (result.type === "good") {
      threatCount.good++;
    }

    if (result.category) {
      categories[result.category] = (categories[result.category] || 0) + 1;
    }
  });

  if (dnsData.error || !dnsData.Answer) {
    score -= 5;
  }
  
  if (score < 0) score = 0;
  if (score > 100) score = 100;

  let grade = 'A+';
  let color = '#00ff9c';
  let summary = 'Site appears safe';

  if (score >= 95) {
    grade = 'A+';
    color = '#00ff9c';
    summary = 'Excellent security rating';
  } else if (score >= 85) {
    grade = 'A';
    color = '#00ff9c';
    summary = 'Good security rating';
  } else if (score >= 75) {
    grade = 'B';
    color = '#7dd3fc';
    summary = 'Generally safe, minor concerns';
  } else if (score >= 65) {
    grade = 'C';
    color = '#ffd60a';
    summary = 'Moderate risk detected';
  } else if (score >= 50) {
    grade = 'D';
    color = '#ff9500';
    summary = 'High risk - proceed with caution';
  } else if (score >= 30) {
    grade = 'F';
    color = '#ff4444';
    summary = 'Very high risk - not recommended';
  } else {
    grade = 'F-';
    color = '#ff0000';
    summary = 'Critical threat detected - avoid';
  }

  return { grade, summary, score, color, threatCount, categories };
}

// Blocklist Management
function getBlocklist(callback) {
  chrome.storage.local.get(['blocklist'], (result) => {
    const blocklist = result.blocklist || [];
    callback(blocklist);
  });
}

function saveBlocklist(blocklist, callback) {
  chrome.storage.local.set({ blocklist: blocklist }, () => {
    if (callback) callback();
  });
}

function addToBlocklist(hostname) {
  if (!hostname) return;
  getBlocklist((blocklist) => {
    if (!blocklist.includes(hostname)) {
      blocklist.push(hostname);
      saveBlocklist(blocklist, () => {
        const btn = document.getElementById('btn-add-site');
        btn.disabled = true;
        btn.textContent = 'Added to Blocklist';
        showSuccessMessage();
      });
    }
  });
}

function showSuccessMessage() {
  const message = document.getElementById('success-message');
  if (message) {
    message.classList.add('show');
    setTimeout(() => {
      message.classList.remove('show');
    }, 2000);
  }
}

// Main Analysis Function
document.addEventListener("DOMContentLoaded", () => {
  const listElement = document.getElementById("details-list");
  const gradeElement = document.getElementById("rating-grade");
  const summaryElement = document.getElementById("rating-summary");
  const scoreElement = document.getElementById("rating-score");
  const threatStats = document.getElementById("threat-stats");
  const loadingIndicator = document.getElementById("loading-indicator");
  const currentSiteElement = document.getElementById("current-site-url");
  const addButton = document.getElementById("btn-add-site");

  const runAnalysis = async () => {
    chrome.tabs.query({ active: true, currentWindow: true }, async (tabs) => {
      const currentTab = tabs[0];
      if (!currentTab || !currentTab.url) {
        gradeElement.textContent = "?";
        summaryElement.textContent = "Could not get tab details.";
        currentSiteElement.textContent = "Unable to get site URL";
        addButton.disabled = true;
        listElement.innerHTML = "";
        return;
      }

      const url = currentTab.url;
      
      // Handle special URLs (chrome://, edge://, etc.)
      if (url.startsWith('chrome://') || url.startsWith('edge://') || url.startsWith('chrome-extension://') || url.startsWith('about:')) {
        gradeElement.textContent = "N/A";
        summaryElement.textContent = "Cannot analyze browser pages";
        currentSiteElement.textContent = url;
        addButton.disabled = true;
        listElement.innerHTML = '<li class="neutral"><span class="result-text">Browser internal pages cannot be analyzed</span></li>';
        return;
      }

      // Show loading state
      loadingIndicator.style.display = "flex";
      listElement.innerHTML = "";

      let hostname = "N/A";
      try {
        const urlObj = new URL(url);
        hostname = urlObj.hostname;
        currentSiteElement.textContent = hostname;
        
        // Check if already in blocklist
        getBlocklist((blocklist) => {
          if (blocklist.includes(hostname)) {
            addButton.disabled = true;
            addButton.textContent = 'Already Blocked';
          } else {
            addButton.onclick = () => addToBlocklist(hostname);
          }
        });
      } catch (e) {
        hostname = "Invalid URL";
        currentSiteElement.textContent = url;
        addButton.disabled = true;
      }

      const localResults = analyzeUrlLocally(url);
      const dnsData = await fetchDnsInfo(hostname);
      const rating = calculateRating(localResults, dnsData);

      // Update UI
      gradeElement.textContent = rating.grade;
      gradeElement.style.color = rating.color;
      summaryElement.textContent = rating.summary;
      scoreElement.textContent = `${rating.score}/100`;
      scoreElement.style.color = rating.color;

      // Update threat stats
      threatStats.innerHTML = `
        <div class="stat-item">
          <span class="stat-badge bad">${rating.threatCount.bad}</span>
          <span class="stat-label">Critical</span>
        </div>
        <div class="stat-item">
          <span class="stat-badge caution">${rating.threatCount.caution}</span>
          <span class="stat-label">Warnings</span>
        </div>
        <div class="stat-item">
          <span class="stat-badge good">${rating.threatCount.good}</span>
          <span class="stat-label">Secure</span>
        </div>
      `;

      // Hide loading
      loadingIndicator.style.display = "none";

      // Display analysis results
      listElement.innerHTML = "";
      
      // Group by category
      const grouped = {};
      localResults.forEach(result => {
        const cat = result.category || "Other";
        if (!grouped[cat]) grouped[cat] = [];
        grouped[cat].push(result);
      });

      // Display by category
      Object.keys(grouped).forEach(category => {
        const categoryHeader = document.createElement("div");
        categoryHeader.className = "category-header";
        categoryHeader.textContent = category;
        listElement.appendChild(categoryHeader);

        grouped[category].forEach(result => {
          const li = document.createElement("li");
          li.className = result.type;
          
          const icon = document.createElement("span");
          icon.className = "result-icon";
          icon.innerHTML = getIconSVG(result.icon || "globe");
          
          const text = document.createElement("span");
          text.className = "result-text";
          text.textContent = result.text;
          
          li.appendChild(icon);
          li.appendChild(text);
          listElement.appendChild(li);
        });
      });

      // DNS information
      if (dnsData.Answer && dnsData.Answer.length > 0) {
        const categoryHeader = document.createElement("div");
        categoryHeader.className = "category-header";
        categoryHeader.textContent = "DNS Information";
        listElement.appendChild(categoryHeader);

        dnsData.Answer.forEach(record => {
          if (record.type === 1) {
            const li = document.createElement("li");
            li.className = "neutral";
            const icon = document.createElement("span");
            icon.className = "result-icon";
            icon.innerHTML = getIconSVG("globe");
            const text = document.createElement("span");
            text.className = "result-text";
            text.textContent = `IP Address: ${record.data}`;
            li.appendChild(icon);
            li.appendChild(text);
            listElement.appendChild(li);
          }
        });
      } else if (dnsData.error) {
        const li = document.createElement("li");
        li.className = "caution";
        const icon = document.createElement("span");
        icon.className = "result-icon";
        icon.innerHTML = getIconSVG("warning");
        const text = document.createElement("span");
        text.className = "result-text";
        text.textContent = `DNS: ${dnsData.error}`;
        li.appendChild(icon);
        li.appendChild(text);
        listElement.appendChild(li);
      }
    });
  };

  runAnalysis();
});
