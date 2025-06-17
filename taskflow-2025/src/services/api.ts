import type { TodoList } from '../types';

const API_BASE_URL = '/api'; // Placeholder for Spring Boot API

// Simulate a delay to mimic network latency
const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export const getLists = async (): Promise<TodoList[]> => {
  console.log('Fetching lists from API...');
  await sleep(500); // Simulate network delay
  // In a real scenario, this would be a fetch call:
  // const response = await fetch(`${API_BASE_URL}/lists`);
  // if (!response.ok) {
  //   throw new Error('Failed to fetch lists');
  // }
  // const data = await response.json();
  // return data;

  // For now, return empty array or mock data if needed for initial testing
  // Getting from localStorage to ensure app still works during transition
  const savedLists = localStorage.getItem("taskflow_lists");
  if (savedLists) {
    return JSON.parse(savedLists);
  }
  return [];
};

export const createList = async (title: string): Promise<TodoList> => {
  console.log(`Creating list: ${title} via API...`);
  await sleep(500);
  // const response = await fetch(`${API_BASE_URL}/lists`, {
  //   method: 'POST',
  //   headers: {
  //     'Content-Type': 'application/json',
  //   },
  //   body: JSON.stringify({ title }),
  // });
  // if (!response.ok) {
  //   throw new Error('Failed to create list');
  // }
  // const newList = await response.json();
  // return newList;

  // Placeholder: create locally and mimic API response
  const newList: TodoList = {
    id: Date.now().toString(), // Backend would generate this
    title,
    tasks: [],
    createdAt: new Date().toISOString(),
  };
  // Simulate saving to backend by updating localStorage for now
  const lists = await getLists();
  const updatedLists = [...lists, newList];
  localStorage.setItem("taskflow_lists", JSON.stringify(updatedLists));
  return newList;
};

export const deleteList = async (listId: string): Promise<void> => {
  console.log(`Deleting list: ${listId} via API...`);
  await sleep(500);
  // const response = await fetch(`${API_BASE_URL}/lists/${listId}`, {
  //   method: 'DELETE',
  // });
  // if (!response.ok) {
  //   throw new Error('Failed to delete list');
  // }

  // Placeholder: delete locally
  let lists = await getLists();
  lists = lists.filter(list => list.id !== listId);
  localStorage.setItem("taskflow_lists", JSON.stringify(lists));
};

export const updateList = async (listToUpdate: TodoList): Promise<TodoList> => {
  console.log(`Updating list: ${listToUpdate.id} via API...`);
  await sleep(500);
  // const response = await fetch(`${API_BASE_URL}/lists/${listToUpdate.id}`, {
  //   method: 'PUT',
  //   headers: {
  //     'Content-Type': 'application/json',
  //   },
  //   body: JSON.stringify(listToUpdate),
  // });
  // if (!response.ok) {
  //   throw new Error('Failed to update list');
  // }
  // const updatedList = await response.json();
  // return updatedList;

  // Placeholder: update locally
  let lists = await getLists();
  lists = lists.map(list => list.id === listToUpdate.id ? listToUpdate : list);
  localStorage.setItem("taskflow_lists", JSON.stringify(lists));
  return listToUpdate;
};
